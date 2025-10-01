//package com.wzz.pinyinsearch.mixin;
//
//import com.wzz.pinyinsearch.core.PinyinSearchHelper;
//import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
//import net.minecraft.world.item.CreativeModeTab;
//import net.minecraft.world.item.ItemStack;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.Unique;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//import java.util.Collection;
//
///**
// * Mixin 到创造模式物品栏界面
// * 这是最稳定的方案，直接修改创造栏的搜索逻辑
// */
//@Mixin(CreativeModeInventoryScreen.class)
//public class CreativeModeInventoryScreenMixin {
//
//    @Shadow
//    private static CreativeModeTab selectedTab;
//
//    @Shadow
//    private net.minecraft.client.gui.components.EditBox searchBox;
//
//    @Shadow
//    private float scrollOffs;
//
//    @Unique
//    private static PinyinSearchHelper<ItemStack> pinyinSearch$helper;
//
//    @Unique
//    private static CreativeModeTab pinyinSearch$lastTab;
//
//    /**
//     * 初始化拼音搜索助手
//     */
//    @Unique
//    private static void pinyinSearch$initHelper() {
//        if (pinyinSearch$helper == null) {
//            pinyinSearch$helper = new PinyinSearchHelper<>(
//                // 获取物品显示名称
//                stack -> stack.getHoverName().getString(),
//                // 获取物品ID(用于@mod过滤)
//                stack -> {
//                    String id = stack.getItem().toString();
//                    // 移除 "Item{" 和 "}" 包装
//                    if (id.startsWith("Item{") && id.endsWith("}")) {
//                        id = id.substring(5, id.length() - 1);
//                    }
//                    return id;
//                }
//            );
//        }
//    }
//
//    /**
//     * Hook refreshSearchResults方法
//     * 完全替换原版搜索逻辑为拼音搜索
//     */
//    @Inject(
//        method = "refreshSearchResults",
//        at = @At("HEAD"),
//        cancellable = true
//    )
//    private void onRefreshSearchResults(CallbackInfo ci) {
//        CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen)(Object)this;
//
//        // 只在有搜索栏的标签页中工作
//        if (!selectedTab.hasSearchBar()) {
//            return;
//        }
//
//        pinyinSearch$initHelper();
//
//        // 如果切换了标签页,重建缓存
//        if (pinyinSearch$lastTab != selectedTab) {
//            Collection<ItemStack> displayItems = selectedTab.getDisplayItems();
//            pinyinSearch$helper.buildCache(displayItems);
//            pinyinSearch$lastTab = selectedTab;
//        }
//
//        // 获取菜单对象
//        CreativeModeInventoryScreen.ItemPickerMenu menu =
//            (CreativeModeInventoryScreen.ItemPickerMenu) screen.getMenu();
//
//        // 清空当前物品列表
//        menu.items.clear();
//
//        String searchText = this.searchBox.getValue();
//
//        // 如果搜索框为空,显示所有物品
//        if (searchText.isEmpty()) {
//            menu.items.addAll(selectedTab.getDisplayItems());
//        } else {
//            // 如果搜索文本以#开头,使用原版标签搜索
//            if (searchText.startsWith("#")) {
//                // 取消hook,让原版逻辑处理标签搜索
//                return;
//            }
//
//            // 使用拼音搜索过滤物品
//            Collection<ItemStack> allItems = selectedTab.getDisplayItems();
//            for (ItemStack item : allItems) {
//                if (pinyinSearch$helper.matches(item, searchText)) {
//                    menu.items.add(item);
//                }
//            }
//        }
//
//        // 重置滚动位置
//        this.scrollOffs = 0.0F;
//        menu.scrollTo(0.0F);
//
//        // 取消原版方法执行
//        ci.cancel();
//    }
//}