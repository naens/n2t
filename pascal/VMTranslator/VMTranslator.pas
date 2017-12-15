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
  ModuleName: string;
  FindPath: string;
  Info: TSearchRec;
  n: integer;
  IsDir: boolean;
begin
  WriteLn('VMTranslator');
  if ParamCount = 1 then
  begin
    Argument := ParamStr(1);
    IsDir := DirectoryExists(Argument);
    if IsDir then
    begin
      FindPath := '*.vm';
      OutputFileName := Argument + '.asm';
    end
    else
    begin
      FindPath := Argument;
      OutputFileName := ChangeFileExt(ParamStr(1), '.asm')
    end;

    if FindFirst(FindPath, faAnyFile, Info) = 0 then
    begin
      WriteLn('Out File Name: ', OutputFileName);
      InitWriter(OutputFileName, OutputFile);
      repeat
        InputFileName := Info.Name;

        ModuleName := copy(InputFileName, 1,
                           LastDelimiter('.', InputFileName) - 1);

        if IsDir then
          WriteInit(OutputFile, ModuleName);

        InitParser(InputFileName, InputFile);
        n := 0;
        while Advance(InputFIle, Command, arg1, arg2) do
        begin
//          writeln(Command, ' ', arg1);
          case Command of
            cArithm: WriteArithm(OutputFile, n, ModuleName, arg1);
            cPush: WritePush(OutputFile, ModuleName, arg1, arg2);
            cPop: WritePop(OutputFile, ModuleName, arg1, arg2);
            cGoto: WriteGoto(OutputFile, ModuleName, arg1);
            cLabel: WriteLabel(OutputFile, ModuleName, arg1);
            cIfGoto: WriteIfGoto(OutputFile, ModuleName, arg1);
            cReturn: WriteReturn(OutputFile, ModuleName);
            cCall: WriteCall(OutputFile, ModuleName, arg1, StrToInt(arg2));
            cFunction:
            begin
              WriteFunction(OutputFile, ModuleName, arg1, StrToInt(arg2))
            end
          end;
        end;
        WriteRestore(OutputFile, ModuleName);
        CloseParser(InputFile);
        n := n + 1
      until FindNext(Info) <> 0;
      FindClose(Info);
      CloseWriter(OutputFile)
    end
  end
end.
