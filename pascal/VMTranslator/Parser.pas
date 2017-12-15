unit Parser;

interface

uses
  SysUtils;

type
  TCommand = (cArithm, cPush, cPop, cLabel, cGoto,
                 cIf, cReturn, cCall, cFunction);

function StringToCommand(s: string): TCommand;

function CommandToString(cmd: TCommand): string;

procedure InitParser(FileName: string; var f: Text);

function Advance(var f: Text; var cmd: TCommand;
                    var arg1: string; var arg2: string): boolean;

procedure CloseParser(var f: Text);

implementation

function StringToCommand(s: string): TCommand;
begin
  case s of
    'push': StringToCommand := cPush;
    'pop': StringToCommand := cPop;
    'label': StringToCommand := cLabel;
    'goto': StringToCommand := cGoto;
    'return': StringToCommand := cReturn;
    'call': StringToCommand := cCall;
    'function': StringToCommand := cFunction
    else
      StringToCommand := cArithm
  end
end;

function CommandToString(cmd: TCommand): string;
begin
  case cmd of
    cArithm: CommandToString := 'arithm';
    cPush: CommandToString := 'push';
    cPop: CommandToString := 'pop';
    cLabel: CommandToString := 'label';
    cGoto: CommandToString := 'goto';
    cIf: CommandToString := 'if';
    cReturn: CommandToString := 'return';
    cCall: CommandToString := 'call';
    cFunction: CommandToString := 'function'
  end
end;

procedure InitParser(FileName: string; var f: Text);
begin
  assign(f, FileName);
  reset(f)
end;

function IsLegalChar(c: char): boolean;
begin
  IsLegalChar := (ord(c) >= ord('a')) and (ord(c) <= ord('z'))
                  or (ord(c) >= ord('0')) and (ord(c) <= ord('9'))
end;

function IsCommand(s: string): boolean;
begin
  IsCommand := IsLegalChar(s[1])
end;

function ReadWord(Source: string; FromPos: integer; var Dest: String): integer;
var
  i: integer;
  len: integer;
begin
  len := length(Source);
  i := FromPos;
  while (i <= len) and IsLegalChar(Source[i]) do
    i := i + 1;
  Dest := copy(Source, FromPos, i - FromPos);

  while (i <= len) and not IsLegalChar(Source[i]) do
    i := i + 1;
  ReadWord := i
end;

function Advance(var f: Text; var cmd: TCommand;
                    var arg1: string; var arg2: string): boolean;
var
  s: string;
  CommandFound: boolean = false;
  CommandString: string;
  i: integer;
begin
  while not CommandFound and not eof(f) do
  begin
    readln(f, s);
    if IsCommand(s) then
      CommandFound := true;
  end;
  i := ReadWord(s, 1, CommandString);
  cmd := StringToCommand(CommandString);
  if cmd = cArithm then
    i := 1;
  i := ReadWord(s, i, arg1);
  i := ReadWord(s, i, arg2);
  Advance := CommandFound
end;

procedure CloseParser(var f: Text);
begin
  close(f)
end;

end.