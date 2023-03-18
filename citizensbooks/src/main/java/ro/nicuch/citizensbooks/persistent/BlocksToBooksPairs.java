package ro.nicuch.citizensbooks.persistent;

import java.util.ArrayList;
import java.util.List;

public class BlocksToBooksPairs {
    private final List<BlockToBookPair> list = new ArrayList<>();

    public List<BlockToBookPair> getList() {
        return this.list;
    }

    public void add(BlockToBookPair blockToBookPair) {
        this.list.add(blockToBookPair);
    }

    public boolean isEmpty() {
        return this.list.isEmpty();
    }
}
