package com.wzz.pinyinsearch.mixin;

import com.wzz.pinyinsearch.core.PinyinSearchHelper;
import net.minecraft.client.searchtree.FullTextSearchTree;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

@Mixin(value = FullTextSearchTree.class)
public class SearchTreeFullTextMixin<T> {

    @Final
    private Function<T, Stream<String>> idGetter;

    @Shadow
    @Final
    private List<T> contents;

    @Unique
    private PinyinSearchHelper<T> pinyinSearch$helper;

    @Unique
    private boolean pinyinSearch$initialized = false;

    /**
     * 初始化拼音搜索助手
     */
    @Unique
    private void pinyinSearch$ensureInitialized() {
        if (!pinyinSearch$initialized) {
            pinyinSearch$initialized = true;
            pinyinSearch$helper = new PinyinSearchHelper<>(
                // 获取显示名称
                item -> {
                    if (item instanceof ItemStack) {
                        return ((ItemStack) item).getHoverName().getString();
                    }
                    // 从idGetter获取名称
                    return idGetter.apply(item)
                        .findFirst()
                        .orElse("");
                },
                // 获取ID
                item -> {
                    if (item instanceof ItemStack) {
                        String id = ((ItemStack) item).getItem().toString();
                        if (id.startsWith("Item{") && id.endsWith("}")) {
                            id = id.substring(5, id.length() - 1);
                        }
                        return id;
                    }
                    return idGetter.apply(item)
                        .findFirst()
                        .orElse("");
                }
            );
            if (contents != null && !contents.isEmpty()) {
                pinyinSearch$helper.buildCache(contents);
            }
        }
    }

    /**
     * Hook搜索方法，添加拼音搜索支持
     */
    @Inject(method = "searchPlainText", at = @At("HEAD"), cancellable = true)
    private void onSearch(String searchText, CallbackInfoReturnable<List<T>> cir) {
        if (searchText == null || searchText.isEmpty()) {
            return;
        }
        // 如果包含中文，使用原版搜索（中文本身就能匹配）
        if (PinyinSearchHelper.containsChinese(searchText)) {
            return;
        }
        // 检查是否全是字母、数字、空格或@符号（可能是拼音搜索）
        if (searchText.matches("^[a-zA-Z0-9\\s@]+$")) {
            pinyinSearch$ensureInitialized();
            // 使用拼音搜索过滤
            List<T> results = new ArrayList<>();
            for (T item : contents) {
                if (pinyinSearch$helper.matches(item, searchText)) {
                    results.add(item);
                }
            }
            // 返回拼音搜索结果
            cir.setReturnValue(results);
        }
    }
}