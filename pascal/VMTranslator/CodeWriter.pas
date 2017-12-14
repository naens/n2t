unit CodeWriter;

interface

uses
  Parser, SysUtils;

procedure InitWriter(FileName: string; var f: Text);

procedure WriteArithm(var f: Text; n: integer;
                      ModuleName: string; Operation: string);

procedure WritePush(var f: Text; ModuleName: string;
                                 Segment: string; Index: string);

procedure WritePop(var f: Text; ModuleName: string;
                                Segment: string; Index: string);

procedure CloseWriter(var f: Text);

implementation

procedure InitWriter(FileName: string; var f: Text);
begin
  assign(f, FileName);
  rewrite(f);
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

procedure WritePush(var f: Text; ModuleName: string;
                                 Segment: string; Index: string);
begin
  writeln(f, '// push ', Segment, ' ', Index);
  WriteVD(f, ModuleName, Segment, Index);
  writeln(f, '@SP');
  writeln(f, 'A=M');
  writeln(f, 'M=D');
  writeln(f, '@SP');
  writeln(f, 'M=M+1');
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

procedure CloseWriter(var f: Text);
begin
  close(f);
end;

end.
