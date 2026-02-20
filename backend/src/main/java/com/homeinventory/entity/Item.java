package com.homeinventory.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 物品实体类
 */
@Entity
@Table(name = "items")
public class Item {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 物品名称
     */
    @Column(nullable = false)
    private String name;
    
    /**
     * 物品描述
     */
    @Column(length = 1000)
    private String description;
    
    /**
     * 物品图片路径（逗号分隔多个图片）
     */
    @Column(length = 2000)
    private String images;
    
    /**
     * 房间名称（如：主卧、客厅）
     */
    @Column(nullable = false)
    private String room;
    
    /**
     * 家具/区域（如：衣柜、书架）
     */
    private String furniture;
    
    /**
     * 具体位置（如：上层抽屉、第二层）
     */
    private String location;
    
    /**
     * 物品分类（如：电子产品、衣物、文件）
     */
    private String category;
    
    /**
     * 标签（逗号分隔，如：重要,常用）
     */
    private String tags;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    /**
     * 是否收藏/置顶
     */
    private Boolean favorite = false;
    
    /**
     * 查看次数
     */
    private Integer viewCount = 0;
    
    // Getter and Setter methods
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getImages() {
        return images;
    }
    
    public void setImages(String images) {
        this.images = images;
    }
    
    public String getRoom() {
        return room;
    }
    
    public void setRoom(String room) {
        this.room = room;
    }
    
    public String getFurniture() {
        return furniture;
    }
    
    public void setFurniture(String furniture) {
        this.furniture = furniture;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Boolean getFavorite() {
        return favorite;
    }
    
    public void setFavorite(Boolean favorite) {
        this.favorite = favorite;
    }
    
    public Integer getViewCount() {
        return viewCount;
    }
    
    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }
}
