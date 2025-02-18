package gruvexp.bbminigames.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MenuRow {

    protected final Inventory inventory;
    public final int startSlot; // slotten i inventoriet som man begynner på
    protected final List<ItemStack> itemList = new ArrayList<>();
    protected final int size; // hvor mange slots som blir tatt opp, inkluderer knapper hvis det er det
    protected int currentPage = 1; // åssen side man er på nå
    protected boolean isVisible = false;
    protected int firstVisibleItem = 0;


    public MenuRow(Inventory inventory, int startSlot, int size) {
        this.inventory = inventory;
        this.startSlot = startSlot;
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public int getStartSlot() {
        return startSlot;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        int count = itemList.size();
        return itemList.size() <= size ? 1 : (count + size - 5) / (size-2);
    }

    public void nextPage() {
        currentPage++;
        goTo(currentPage);
    }

    public void prevPage() {
        currentPage--;
        goTo(currentPage);
    }

    public void updatePage() {
        goTo(currentPage);
    }

    public List<ItemStack> getItems() {
        return itemList;
    }

    protected void goTo(int page) {
        int totalPages = getTotalPages();
        page = Math.min(page, totalPages);
        firstVisibleItem = 0;
        if (totalPages == 1) { // alle itemsene fyller heile rada
            for (int i = 0; i < size; i++) {
                ItemStack item = i < itemList.size() ? itemList.get(i) : null;
                inventory.setItem(startSlot + i, item);
            }
            return;
        }
        if (page == 1) { // rada fylles med første side bortsett fra en next knapp på slutten
            for (int i = 0; i < size - 1; i++) {
                ItemStack item = i < itemList.size() ? itemList.get(i) : null;
                inventory.setItem(startSlot + i, item);
            }
            inventory.setItem(startSlot + size - 1, Menu.NEXT);
            return;
        }
        firstVisibleItem = size + (size - 2)*(page - 2); // første element på den sida
        setItem(0, Menu.PREV); // en prev knapp først, deretter fylles rada opp bortsett fra den siste hvis det er en midtside, da blir det en next på slutten
        for (int i = 0; i < size - 2; i++) {
            int targetSlot = 1 + i; // begynner på slot 2 pga nr 1 er for PREV knappen
            ItemStack item = firstVisibleItem + i < itemList.size() ? itemList.get(firstVisibleItem + i) : null;
            setItem(targetSlot, item);
        }
        if (page == totalPages) {
            ItemStack item = firstVisibleItem + size - 1 < itemList.size() ? itemList.get(firstVisibleItem + size - 1) : null;
            setItem(size - 1, item);
        } else {
            setItem(size - 1, Menu.NEXT);
        }

    }

    private void setItem(int index, ItemStack item) {
        inventory.setItem(startSlot + index, item);
    }

    public void addItem(ItemStack item) {
        itemList.add(item);
        if (isVisible && currentPage == getTotalPages()) {
            updatePage();
        }
    }

    public void removeItem(ItemStack item) {
        itemList.remove(item);
        updatePage();
    }

    public void show() {
        isVisible = true;
        updatePage();
    }

    public void hide() {
        isVisible = false;
    }

    // xxxxxx
    // 111122

    // xxxxxx--     xxxxxxxx
    // 111222       11112222

    // xxxxxxx--    xxxxxxxxx
    // 111222333    111122233

    // xxxxxxxxxxx
    // 11112222
}
