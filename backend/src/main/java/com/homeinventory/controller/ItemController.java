package com.homeinventory.controller;

import com.homeinventory.entity.Item;
import com.homeinventory.service.ItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")  // 允许跨域，生产环境需要限制
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }
    
    /**
     * 创建物品
     */
    @PostMapping
    public ResponseEntity<Item> createItem(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("room") String room,
            @RequestParam(value = "furniture", required = false) String furniture,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) throws IOException {
        
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setRoom(room);
        item.setFurniture(furniture);
        item.setLocation(location);
        item.setCategory(category);
        item.setTags(tags);
        
        return ResponseEntity.ok(itemService.createItem(item, images));
    }
    
    /**
     * 更新物品
     */
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("room") String room,
            @RequestParam(value = "furniture", required = false) String furniture,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) throws IOException {
        
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setRoom(room);
        item.setFurniture(furniture);
        item.setLocation(location);
        item.setCategory(category);
        item.setTags(tags);
        
        return ResponseEntity.ok(itemService.updateItem(id, item, images));
    }
    
    /**
     * 删除物品
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取单个物品
     */
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItem(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItem(id));
    }
    
    /**
     * 获取所有物品
     */
    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }
    
    /**
     * 按房间查找
     */
    @GetMapping("/room/{room}")
    public ResponseEntity<List<Item>> getItemsByRoom(@PathVariable String room) {
        return ResponseEntity.ok(itemService.getItemsByRoom(room));
    }
    
    /**
     * 搜索物品
     */
    @GetMapping("/search")
    public ResponseEntity<List<Item>> searchItems(@RequestParam String keyword) {
        return ResponseEntity.ok(itemService.searchItems(keyword));
    }
    
    /**
     * 获取所有房间
     */
    @GetMapping("/rooms")
    public ResponseEntity<List<String>> getAllRooms() {
        return ResponseEntity.ok(itemService.getAllRooms());
    }
    
    /**
     * 获取房间的家具列表
     */
    @GetMapping("/rooms/{room}/furniture")
    public ResponseEntity<List<String>> getFurnitureByRoom(@PathVariable String room) {
        return ResponseEntity.ok(itemService.getFurnitureByRoom(room));
    }
    
    /**
     * 获取收藏的物品
     */
    @GetMapping("/favorites")
    public ResponseEntity<List<Item>> getFavoriteItems() {
        return ResponseEntity.ok(itemService.getFavoriteItems());
    }
    
    /**
     * 获取最近热门物品
     */
    @GetMapping("/popular")
    public ResponseEntity<List<Item>> getPopularItems() {
        return ResponseEntity.ok(itemService.getPopularItems());
    }
    
    /**
     * 切换收藏状态
     */
    @PostMapping("/{id}/favorite")
    public ResponseEntity<Item> toggleFavorite(@PathVariable Long id, @RequestParam Boolean favorite) {
        return ResponseEntity.ok(itemService.toggleFavorite(id, favorite));
    }
    
    /**
     * 增加查看次数
     */
    @PostMapping("/{id}/view")
    public ResponseEntity<Void> incrementViewCount(@PathVariable Long id) {
        itemService.incrementViewCount(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalItems", itemService.getAllItems().size());
        stats.put("rooms", itemService.getAllRooms());
        return ResponseEntity.ok(stats);
    }
}
