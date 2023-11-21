package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 新增购物车
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        // 判断当前加入的商品是否存在了
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        log.info("当前用户id：{}", userId);
        shoppingCart.setUserId(userId);

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        // 如果存在了，只需要将数量+1

        if (list != null && list.size() > 0) {
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(cart);
            return;
        }else {
            // 如果不存在，需要插入一条数据

            // 判断下次添加的是单品还是套餐
            Long dishId = shoppingCartDTO.getDishId();

            if (dishId != null) {
                // 本次添加到购物车的是单品
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());

            }else {
                Long setmealId = shoppingCartDTO.getSetmealId();
                // 本次添加到购物车的是套餐
                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);

        }

    }

    /**
     * 查询购物车列表
     * @return
     */
    @Override
    public List<ShoppingCart> showShoppingCart() {
        // 获取当前用户id
        Long currentId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(currentId)
                .build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;
    }

    /**
     * 清空购物车
     */
    @Override
    public void clean() {
        Long currentId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(currentId);
    }

    /**
     * 减少购物车中的商品数量
     * @param shoppingCartDTO
     */
    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        log.info("当前用户id：{}", userId);
        shoppingCart.setUserId(userId);

        // 查询当前登录用户的购物车中是否存在该商品
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list != null && list.size() > 0) {
            ShoppingCart cart = list.get(0);
            // 判断当前商品的数量是否大于1
            if (cart.getNumber() > 1) {
                cart.setNumber(cart.getNumber() - 1);
                shoppingCartMapper.updateNumberById(cart);
            } else {
                // 如果等于1，直接删除
                shoppingCartMapper.deleteByUserId(userId);
            }
        }
    }
}
