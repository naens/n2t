unit CodeWriter;

interface

uses
  Parser, SysUtils;

procedure InitWriter(FileName: string; var f: Text);

procedure WriteInit(var f: Text; n: integer);

procedure WriteArithm(var f: Text; n: integer;
                      ModuleName: string; Operation: string);

procedure WritePush(var f: Text; ModuleName: string;
                                 Segment: string; Index: string);

procedure WritePop(var f: Text; ModuleName: string;
                                Segment: string; Index: string);

procedure WriteGoto(var f: Text; FunctionName: string; LabelName: string);

procedure WriteLabel(var f: Text; FunctionName: string; LabelName: string);

procedure WriteIfGoto(var f: Text; FunctionName: string; LabelName: string);

procedure WriteReturn(var f: Text; Name: string);

procedure WriteCall(var f: Text; n:integer;
                                FunctionName: string; NumArgs: integer);

procedure WriteEnd(var f: Text; Name: string);

procedure WriteRestore(var f: Text; Name: string);

procedure WriteFunction(var f: Text; FunctionName: string; NumVars: integer);

procedure CloseWriter(var f: Text);

implementation

procedure InitWriter(FileName: string; var f: Text);
begin
  assign(f, FileName);
  rewrite(f);
end;

procedure WriteInit(var f: Text; n: integer);
begin
  writeln(f, '// init');
  writeln(f, '@256');
  writeln(f, 'D=A');
  writeln(f, '@SP');
  writeln(f, 'M=D');
  writeln(f);
  WriteCall(f, n, 'Sys.init', 0);
end;

procedure WriteUnary(var f: Text; Operation: string);
begin
  writeln(f, '@SP');
  writeln(f, 'A=M-1');
  if CompareStr(Operation, 'neg') = 0 then
    writeln(f, 'M=-M')
  else
    writeln(f, 'M=!M');
  writeln(f)
end;

procedure WriteRelational(var f: Text; n: integer;
                      ModuleName: string; Operation: string);
begin
  writeln(f, 'D=M-D');
  writeln(f, '@relat.mt.', LowerCase(ModuleName), '.', n);
  writeln(f, 'D;J', UpperCase(Operation));
  writeln(f, '@SP');
  writeln(f, 'A=M-1');
  writeln(f, 'M=0');
  writeln(f, '@relat.end.', LowerCase(ModuleName), '.', n);
  writeln(f, '0;JMP');
  writeln(f, '(relat.mt.', LowerCase(ModuleName), '.', n, ')');
  writeln(f, '@SP');
  writeln(f, 'A=M-1');
  writeln(f, 'M=-1');
  writeln(f, '(relat.end.', LowerCase(ModuleName), '.', n, ')');
end;

procedure WriteBinary(var f: Text; n: integer;
                      ModuleName: string; Operation: string);
begin
  writeln(f, '@SP');
  writeln(f, 'M=M-1');
  writeln(f, 'A=M');
  writeln(f, 'D=M');
  writeln(f, 'A=A-1');
  case Operation of
    'eq', 'gt', 'lt': WriteRelational(f, n, ModuleName, Operation);
    'add': writeln(f, 'M=M+D');
    'sub': writeln(f, 'M=M-D');
    'and': writeln(f, 'M=M&D');
    'or': writeln(f, 'M=M|D');
  end;
  writeln(f);
end;

procedure WriteArithm(var f: Text; n: integer;
                      ModuleName: string; Operation: string);
begin
  writeln(f, '// arithm ', Operation);
  if (CompareStr(Operation, 'neg') = 0)
      or (CompareStr(Operation, 'not') = 0) then
    WriteUnary(f, Operation)
  else
    WriteBinary(f, n, ModuleName, Operation)
end;

function SegmentConstant(Segment: string): string;
begin
  case Segment of
    'local': SegmentConstant := 'LCL';
    'argument': SegmentConstant := 'ARG';
    'this': SegmentConstant := 'THIS';
    'that': SegmentConstant := 'THAT'
  end
end;

procedure WriteVD(var f: Text; ModuleName: string;
                               Segment: string; Index: string);
begin
  case Segment of
    'temp':
    begin
      writeln(f, '@5');
      writeln(f, 'D=A');
      writeln(f, '@', Index);
      writeln(f, 'A=A+D');
      writeln(f, 'D=M');
    end;
    'constant':
    begin
      writeln(f, '@', Index);
      writeln(f, 'D=A');
    end;
    'static':
    begin
      writeln(f, '@', ModuleName, '.', Index);
      writeln(f, 'D=M');
    end;
    'pointer':
    begin
      case Index of
        '0': writeln(f, '@THIS');
        '1': writeln(f, '@THAT');
      end;
      writeln(f, 'D=M');
    end;
    else
    begin
      writeln(f, '@', SegmentConstant(Segment));
      writeln(f, 'D=M');
      writeln(f, '@', Index);
      writeln(f, 'A=A+D');
      writeln(f, 'D=M');
    end;
  end;
end;

procedure WritePushExpr(var f: Text; Expr: string);
begin
  writeln(f, '@SP');
  writeln(f, 'A=M');
  writeln(f, 'M=', Expr);
  writeln(f, '@SP');
  writeln(f, 'M=M+1');
end;

procedure WritePush(var f: Text; ModuleName: string;
                                 Segment: string; Index: string);
begin
  writeln(f, '// push ', Segment, ' ', Index);
  WriteVD(f, ModuleName, Segment, Index);
  WritePushExpr(f, 'D');
  writeln(f)
end;

procedure WriteDV(var f: Text; ModuleName: string;
                               Segment: string; Index: string);
begin
  case Segment of
    'static':
    begin
      writeln(f, '@', ModuleName, '.', Index);
      writeln(f, 'M=D')
    end;
    'pointer':
    begin
      case Index of
        '0': writeln(f, '@THIS');
        '1': writeln(f, '@THAT')
      end;
      writeln(f, 'M=D')
    end
    else
    begin
      writeln(f, '@R13');
      writeln(f, 'M=D');
      if CompareStr(Segment, 'temp') = 0 then
      begin
        writeln(f, '@5');
        writeln(f, 'D=A')
      end
      else
      begin
        writeln(f, '@', SegmentConstant(Segment));
        writeln(f, 'D=M')
      end;
      writeln(f, '@', Index);
      writeln(f, 'D=A+D');
      writeln(f, '@R14');
      writeln(f, 'M=D');
      writeln(f, '@R13');
      writeln(f, 'D=M');
      writeln(f, '@R14');
      writeln(f, 'A=M');
      writeln(f, 'M=D');
      writeln(f)
    end
  end
end;

procedure WritePop(var f: Text; ModuleName: string;
                                Segment: string; Index: string);
begin
  writeln(f, '// pop ', Segment, ' ', Index);
  writeln(f, '@SP');
  writeln(f, 'M=M-1');
  writeln(f, 'A=M');
  writeln(f, 'D=M');
  WriteDV(f, ModuleName, Segment, Index);
  writeln(f);
end;

procedure WriteGoto(var f: Text; FunctionName: string; LabelName: string);
begin
  writeln(f, '// goto ', LabelName);
  writeln(f, '@', FunctionName, '$', LabelName);
  writeln(f, '0;JMP');
  writeln(f);
end;

procedure WriteLabel(var f: Text; FunctionName: string; LabelName: string);
begin
  writeln(f, '// label ', LabelName);
  writeln(f, '(', FunctionName, '$', LabelName, ')');
  writeln(f);
end;

procedure WriteIfGoto(var f: Text; FunctionName: string; LabelName: string);
begin
  writeln(f, '// if-goto ', LabelName);
  writeln(f, '@SP');
  writeln(f, 'M=M-1');
  writeln(f, 'A=M');
  writeln(f, 'D=M');
  writeln(f, '@', FunctionName, '$', LabelName);
  writeln(f, 'D;JNE');
  writeln(f);
end;

procedure WriteReturn(var f: Text; Name: string);
begin
  writeln(f, '// return to ', Name, '$restore');
  writeln(f, '@', Name, '$restore');
  writeln(f, '0;JMP');
  writeln(f);
end;

procedure WriteCall(var f: Text; n:integer; 
                    FunctionName: string; NumArgs: integer);
var
  ReturnLabel: string;
begin
  writeln(f, '// call ', FunctionName, ' ', NumArgs);
  ReturnLabel := FunctionName + '$ret.' + IntToStr(n);

  {push return address}
  writeln(f, '@', ReturnLabel); 
  writeln(f, 'D=A');
  WritePushExpr(f, 'D');

  {push LCL}
  writeln(f, '@LCL');
  writeln(f, 'D=M');
  WritePushExpr(f, 'D');

  {push ARG}
  writeln(f, '@ARG');
  writeln(f, 'D=M');
  WritePushExpr(f, 'D');

  {push THIS}
  writeln(f, '@THIS');
  writeln(f, 'D=M');
  WritePushExpr(f, 'D');

  {push THAT}
  writeln(f, '@THAT');
  writeln(f, 'D=M');
  WritePushExpr(f, 'D');

  {LCL=SP}
  writeln(f, '@SP');
  writeln(f, 'D=M');
  writeln(f, '@LCL');
  writeln(f, 'M=D');

  {ARG=SP-5-NumArgs}
  writeln(f, '@5');
  writeln(f, 'D=D-A');		{D=[LCL]-5}
  writeln(f, '@', NumArgs);
  writeln(f, 'D=D-A');
  writeln(f, '@ARG');
  writeln(f, 'M=D');

  {goto function}
  writeln(f, '@', FunctionName);
  writeln(f, '0;JMP');

  writeln(f, '(', ReturnLabel, ')');

  writeln(f);
end;

procedure WriteEnd(var f: Text; Name: string);
begin
  writeln(f, '(', Name, '$end)');
  writeln(f, '@', Name, '$end');
  writeln(f, '0;JMP');
  writeln(f);
end;

procedure WriteRestore(var f: Text; Name: string);
begin
  writeln(f, '(', Name, '$restore)');

  writeln(f, '@ARG');		{save ARG}
  writeln(f, 'D=M');
  writeln(f, '@R14');
  writeln(f, 'M=D');

  writeln(f, '@LCL');
  writeln(f, 'D=M-1');  {D=[LCL]-1}
  writeln(f, '@R13');
  writeln(f, 'AM=D');	  {[R13]=[LCL]-1}
  writeln(f, 'D=M');    {D=[[LCL]-1]}
  writeln(f, '@THAT');
  writeln(f, 'M=D');

  writeln(f, '@R13');
  writeln(f, 'D=M-1');  {D=[LCL]-2}
  writeln(f, 'AM=D');		{[R13]=[LCL]-2}
  writeln(f, 'D=M');    {D=[[LCL]-2]}
  writeln(f, '@THIS');
  writeln(f, 'M=D');

  writeln(f, '@R13');
  writeln(f, 'D=M-1');  {D=[LCL]-3}
  writeln(f, 'AM=D');		{[R13]=[LCL]-3}
  writeln(f, 'D=M');    {D=[[LCL]-3]}
  writeln(f, '@ARG');
  writeln(f, 'M=D');

  writeln(f, '@R13');
  writeln(f, 'D=M-1');  {D=[LCL]-4}
  writeln(f, 'AM=D');		{[R13]=[LCL]-4}
  writeln(f, 'D=M');    {D=[[LCL]-4]}
  writeln(f, '@LCL');
  writeln(f, 'M=D');

  writeln(f, '@R13');		{save return address}
  writeln(f, 'A=M-1');
  writeln(f, 'D=M');		{D=[[LCL]-5]}
  writeln(f, '@R13');
  writeln(f, 'M=D');		{[R13]=[[LCL]-15]}

  writeln(f, '@SP');		{set return value}
  writeln(f, 'A=M-1');
  writeln(f, 'D=M');
  writeln(f, '@R14');
  writeln(f, 'A=M');
  writeln(f, 'M=D');		{[[old-ARG]]=[[SP]-1]}

  writeln(f, '@R14');   {set [SP] after return value}
  writeln(f, 'D=M+1');
  writeln(f, '@SP');
  writeln(f, 'M=D');    {[SP]=[old-ARG]+1}

  writeln(f, '@R13');
  writeln(f, 'A=M');
  writeln(f, '0;JMP');
  writeln(f);
end;


procedure WriteFunction(var f: Text; 
                                FunctionName: string; NumVars: integer);
var
  i: integer;
begin
  writeln(f, '// function ', FunctionName, ' ', NumVars);
  writeln(f, '(', FunctionName, ')');

  for i := 1 to NumVars do
    WritePushExpr(f, '0');

  writeln(f);
end;

procedure CloseWriter(var f: Text);
begin
  close(f);
end;

end.
