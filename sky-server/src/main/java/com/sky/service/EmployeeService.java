package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
//  架构解藕，可以轻松替换实现类而不影响上层代码，也方便也单元测试
    Employee login(EmployeeLoginDTO employeeLoginDTO);
    /**
     * 新增员工
     * @param employeeDTO
     */
    void save(EmployeeDTO employeeDTO);

    /**
     *
     * @param employeePageQueryDTO
     * @return
     */
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 启用禁用员工学习
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 根据用户id获取信息
     * @param id
     * @return
     */
    Employee getById(Long id);

    /**
     * 编辑用户信息
     * @param employeeDTO
     */
    void update(EmployeeDTO employeeDTO);
}
