package com.homeinventory.service;

import com.homeinventory.entity.Item;
import com.homeinventory.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class ItemService {
    
    private final ItemRepository itemRepository;
    private static final String UPLOAD_DIR = "uploads/";
    
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }
    
    /**
     * 创建物品
     */
    @Transactional
    public Item createItem(Item item, List<MultipartFile> images) throws IOException {
        // 处理图片上传
        if (images != null && !images.isEmpty()) {
            String imagePaths = saveImages(images);
            item.setImages(imagePaths);
        }
        return itemRepository.save(item);
    }
    
    /**
     * 更新物品
     */
    @Transactional
    public Item updateItem(Long id, Item item, List<MultipartFile> images) throws IOException {
        Item existing = itemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("物品不存在"));
        
        existing.setName(item.getName());
        existing.setDescription(item.getDescription());
        existing.setRoom(item.getRoom());
        existing.setFurniture(item.getFurniture());
        existing.setLocation(item.getLocation());
        existing.setCategory(item.getCategory());
        existing.setTags(item.getTags());
        
        // 处理新图片
        if (images != null && !images.isEmpty()) {
            String imagePaths = saveImages(images);
            existing.setImages(imagePaths);
        }
        
        return itemRepository.save(existing);
    }
    
    /**
     * 删除物品
     */
    @Transactional
    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }
    
    /**
     * 获取单个物品
     */
    public Item getItem(Long id) {
        return itemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("物品不存在"));
    }
    
    /**
     * 获取所有物品
     */
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }
    
    /**
     * 按房间查找
     */
    public List<Item> getItemsByRoom(String room) {
        return itemRepository.findByRoomOrderByUpdatedAtDesc(room);
    }
    
    /**
     * 搜索物品
     */
    public List<Item> searchItems(String keyword) {
        return itemRepository.searchByKeyword(keyword);
    }
    
    /**
     * 获取所有房间
     */
    public List<String> getAllRooms() {
        return itemRepository.findAllRooms();
    }
    
    /**
     * 获取房间的家具列表
     */
    public List<String> getFurnitureByRoom(String room) {
        return itemRepository.findFurnitureByRoom(room);
    }
    
    /**
     * 获取收藏的物品
     */
    public List<Item> getFavoriteItems() {
        return itemRepository.findByFavoriteTrueOrderByUpdatedAtDesc();
    }
    
    /**
     * 获取最近热门物品
     */
    public List<Item> getPopularItems() {
        return itemRepository.findTop5ByOrderByViewCountDesc();
    }
    
    /**
     * 切换收藏状态
     */
    @Transactional
    public Item toggleFavorite(Long id, Boolean favorite) {
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("物品不存在"));
        item.setFavorite(favorite);
        return itemRepository.save(item);
    }
    
    /**
     * 增加查看次数
     */
    @Transactional
    public void incrementViewCount(Long id) {
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("物品不存在"));
        item.setViewCount(item.getViewCount() + 1);
        itemRepository.save(item);
    }
    
    /**
     * 保存图片
     */
    private String saveImages(List<MultipartFile> images) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        StringBuilder paths = new StringBuilder();
        for (int i = 0; i < images.size(); i++) {
            MultipartFile file = images.get(i);
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            paths.append("/uploads/").append(filename);
            if (i < images.size() - 1) {
                paths.append(",");
            }
        }
        return paths.toString();
    }
}
