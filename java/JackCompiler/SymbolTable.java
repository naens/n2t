import java.util.*;

class SymbolTable {

    /* entry: name,type,kind,index */
    private static class Entry {
        String type;
        Kind kind;
        int index;
    }
        
    private HashMap<String, Entry> classMap = null;
    private HashMap<String, Entry> procMap = null;

    public SymbolTable() {
        classMap = new HashMap<String, Entry>();
    }

    public void startSubroutine() {
        procMap = new HashMap<String, Entry>();
    }

    private Entry newEntry(String type, Kind kind, int index) {
        Entry entry = new Entry();
        entry.type = type;
        entry.kind = kind;
        entry.index = index;
        return entry;
    }

    public int define(String name, String type, Kind kind) {
        int c = -1;
        switch (kind) {
        case STATIC:
        case FIELD:
            c = varCount(kind, classMap);
            classMap.put(name, newEntry(type, kind, c));
            break;
        case LOCAL:
        case ARGUMENT:
            c = varCount(kind, procMap);
            procMap.put(name, newEntry(type, kind, c));
            break;
        }
        return c;
    }

    public int fields() {
        return varCount(Kind.FIELD, classMap);
    }

    private int varCount(Kind kind, HashMap<String, Entry> map) {
        int result = 0;
        for (Entry entry : map.values()) {
            if (entry.kind == kind) {
                result++;
            }
        }
        return result;
    }

    private Entry getEntry(String name) {
        Entry entry = procMap.get(name);
        return entry != null ? entry : classMap.get(name);
    }

    public boolean isClass(String name) {
        return getEntry(name) == null;
    }

    public Kind kindOf(String name) {
        return getEntry(name).kind;
    }

    public String typeOf(String name) {
        return getEntry(name).type;
    }

    public int indexOf(String name) {
        return getEntry(name).index;
    }
}
