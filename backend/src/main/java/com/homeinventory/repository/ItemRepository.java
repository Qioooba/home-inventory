package com.homeinventory.repository;

import com.homeinventory.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * 按房间查找物品
     */
    List<Item> findByRoomOrderByUpdatedAtDesc(String room);

    /**
     * 按名称模糊搜索
     */
    @Query("SELECT i FROM Item i WHERE i.name LIKE %:keyword% OR i.description LIKE %:keyword% ORDER BY i.updatedAt DESC")
    List<Item> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 按分类查找
     */
    List<Item> findByCategoryOrderByUpdatedAtDesc(String category);

    /**
     * 按房间和家具查找
     */
    List<Item> findByRoomAndFurnitureOrderByUpdatedAtDesc(String room, String furniture);

    /**
     * 获取所有房间列表（去重）
     */
    @Query("SELECT DISTINCT i.room FROM Item i ORDER BY i.room")
    List<String> findAllRooms();

    /**
     * 获取指定房间的所有家具（去重）
     */
    @Query("SELECT DISTINCT i.furniture FROM Item i WHERE i.room = :room AND i.furniture IS NOT NULL ORDER BY i.furniture")
    List<String> findFurnitureByRoom(@Param("room") String room);

    /**
     * 获取收藏的物品
     */
    List<Item> findByFavoriteTrueOrderByUpdatedAtDesc();

    /**
     * 获取查看次数最多的物品（最近热门）
     */
    List<Item> findTop5ByOrderByViewCountDesc();
}
