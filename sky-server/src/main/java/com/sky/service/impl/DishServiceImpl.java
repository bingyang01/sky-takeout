package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;
    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 新增菜品
     * @param dishDTO
     */
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO){

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        List<DishFlavor> flavors = dishDTO.getFlavors();
//        向菜品表插入1条数据
        dishMapper.insert(dish);

        Long dishId = dish.getId();
//        向口味表插入n条数据
        if(flavors != null && flavors.size() >0){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });

            dishFlavorMapper.insertBatch(flavors);

        }

    }

    /**
     * 根据id删除菜品
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
//   判断菜品是否能删除1:是否启用
        for(Long id: ids){
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
//   判断菜品是否能删除2: 是否关联套餐

        List<Long> setMealIds =setmealDishMapper.getSetMealIdsByDishID(ids);
        if(setMealIds != null && setMealIds.size() > 0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
////   删除菜品：从菜品表中删除数据
//        for(Long id: ids){
//            dishMapper.deleteById(id);
////   从口味表中删除菜品关联的口味数据
//            dishFlavorMapper.deleteByDishId(id);
//
//        }
//        删除优化写法
        dishMapper.deleteByIds(ids);
        dishFlavorMapper.delteByDishIds(ids);



    }

    /**
     * 根据id查询菜品和对应的口味数据
     * @param id
     * @return
     */
    public DishVO getByIdWithFlavor(Long id) {
//   查询菜品表
        Dish dish = dishMapper.getById(id);
//   根据菜品id查询口味
        List<DishFlavor> dishFlavors=dishFlavorMapper.getByDishId(id);
//    将查询到的数据封装到vo
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);

        return dishVO;
    }
    /**
     * 修改菜品信息
     * @param dishDTO
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
//  修改菜品信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);
//  删除菜品相关口味信息
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
//  插入口味信息
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0){
            flavors.forEach(dishFlavor ->{
                        dishFlavor.setDishId(dishDTO.getId());
                    });
            dishFlavorMapper.insertBatch(flavors);

        }

    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }
    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);

    }
}
