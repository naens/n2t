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

    public void define(String name, String type, Kind kind) {
        System.out.println(String.format("def %s: %s, %s", name, type, kind));
        switch (kind) {
        case STATIC:
        case FIELD:
            classMap.put(name, newEntry(type, kind, varCount(kind, classMap)));
            break;
        case LOCAL:
        case ARGUMENT:
            procMap.put(name, newEntry(type, kind, varCount(kind, procMap)));
            break;
        }
    }

    public void println(String name) {
        Entry entry = getEntry(name);
        if (entry == null) {
            System.out.println(String.format("ident: unknown %s", name));
        } else {
            System.out.println(String.format("ident: %s: %s, %s[%d]",
                    name, entry.type, entry.kind, entry.index));
        }
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
