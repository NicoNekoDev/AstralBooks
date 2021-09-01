package ro.nicuch.citizensbooks.utils;

import org.bukkit.inventory.ItemStack;

import java.nio.file.Path;

public class BookLink {
    private final ItemStack book;
    private final Path link;

    public BookLink(ItemStack book, Path link) {
        this.book = book;
        this.link = link;
    }

    public final ItemStack getBook() {
        return this.book;
    }

    public final Path getLink() {
        return this.link;
    }
}
