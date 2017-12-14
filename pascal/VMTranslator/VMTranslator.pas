program VMTranslator;

uses
  SysUtils, Parser, CodeWriter;

var
  infn: string;
  outfn: string;
  inf: Text;
  outf: Text;
  cmd: TCommand;
  arg1: string;
  arg2: string;
  DotPosition: integer;
  ModuleName: string;
  n: integer;
begin
  writeln('VMTranslator');
  if ParamCount = 1 then
  begin
    infn := ParamStr(1);
    if FileExists(infn) then
    begin
      outfn := ChangeFileExt(ParamStr(1), '.asm');

      ModuleName := copy(outfn, 1, LastDelimiter('.', outfn) - 1);

      writeln('Out File Name: ', outfn);
      InitParser(infn, inf);
      InitWriter(outfn, outf);
      n := 0;
      while Advance(inf, cmd, arg1, arg2) do
      begin
        case cmd of
          cArithm: WriteArithm(outf, n, ModuleName, arg1);
          cPush: WritePush(outf, ModuleName, arg1, arg2);
          cPop: WritePop(outf, ModuleName, arg1, arg2);
       end;
       n := n + 1;
      end;
      CloseParser(inf);
      CloseWriter(outf)
    end
    else
      writeln('No File ', infn)
  end
end.
