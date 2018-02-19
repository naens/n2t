program VMTranslator;

uses
  SysUtils,
  Parser, CodeWriter;

var
  Argument: string;
  InputFileName: string;
  OutputFileName: string;
  InputFile: Text;
  OutputFile: Text;
  Command: TCommand;
  arg1: string;
  arg2: string;
  Temp, Name, FunctionName, ModuleName: string;
  WorkingDir: string;
  FindPath: string;
  Info: TSearchRec;
  n: integer;
  IsDir: boolean;
begin
  if ParamCount = 1 then
  begin
    Argument := ParamStr(1);
    IsDir := DirectoryExists(Argument);
    if IsDir then
    begin
      while Argument[length(Argument)] = '/' do
        Argument := copy(Argument, 1, length(Argument) - 1);
      FindPath := '*.vm';
      Name := ExtractFileName(Argument);
      OutputFileName := ExpandFileName(Name + '.asm');
      WorkingDir := Argument;
    end
    else
    begin
      FindPath := Argument;
      Temp := ExtractFileName(Argument);
      Name := copy(Temp, 1, LastDelimiter('.', Temp) - 1);
      OutputFileName := ExpandFileName(ChangeFileExt(Name, '.asm'));
      WorkingDir := ExtractFileDir(Argument);
    end;
    
    writeln('Name=', Name);
    SetCurrentDir(WorkingDir);
    writeln('WorkingDir=', WorkingDir);
    writeln('OutputFileName=', OutputFileName);

    if FindFirst(FindPath, faAnyFile, Info) = 0 then
    begin
      InitWriter(OutputFileName, OutputFile);
      if IsDir then
        WriteInit(OutputFile, 0);
      repeat
        InputFileName := Info.Name;
        writeln('InputFileName=', InputFileName);

        ModuleName := copy(InputFileName, 1,
                           LastDelimiter('.', InputFileName) - 1);

        n := 1;
	FunctionName := 'Sys.init';
        InitParser(InputFileName, InputFile);
        while Advance(InputFIle, Command, arg1, arg2) do
        begin
          case Command of
            cArithm: WriteArithm(OutputFile, n, ModuleName, arg1);
            cPush: WritePush(OutputFile, ModuleName, arg1, arg2);
            cPop: WritePop(OutputFile, ModuleName, arg1, arg2);
            cGoto: WriteGoto(OutputFile, FunctionName, arg1);
            cLabel: WriteLabel(OutputFile, FunctionName, arg1);
            cIfGoto: WriteIfGoto(OutputFile, FunctionName, arg1);
            cReturn: WriteReturn(OutputFile, Name);
            cCall: WriteCall(OutputFile, n, arg1, StrToInt(arg2));
            cFunction: 
            begin
              FunctionName := arg1;
              WriteFunction(OutputFile, arg1, StrToInt(arg2))
            end
          end;
          n := n + 1
        end;
        CloseParser(InputFile);
      until FindNext(Info) <> 0;
      FindClose(Info);
      WriteEnd(OutputFile, Name);
      WriteRestore(OutputFile, Name);
      CloseWriter(OutputFile)
    end
  end
end.
