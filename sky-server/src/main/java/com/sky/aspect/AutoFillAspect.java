package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 切入点
     * 参数意义：返回值是所有的，包下面的所有类的方法及各种参类型
     */
    /**
     * 分开写autoFillPointCut() 和 autoFill()
     * 职责单一，一个方法只做一件事，autoFillPointCut() 便于复用、修改、扩展，不影响已有的通知逻辑。
     * 合并的话，如果后期有多个通知方法要重新用这个切点，不方便维护
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}
/**
 * 前置通知 当匹配到autoFillPointCut()方法的时候，可以开始执行
 */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint ){
        log.info("开始进行公共字段填充");

//        1. 获取到当前被拦截方法的数据库操作类型：通过反射机制，在 AOP 切面中读取当前执行方法的注解 @AutoFill，并获取其操作类型（INSERT 或 UPDATE），以决定后续执行哪个字段填充逻辑。
//        getSignature 获得这个方法的签名/元数据，默认返回signature类型，所以要强转
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
//        getMethod()：返回的是 java.lang.reflect.Method，即 Java 反射的方法对象。
//        getAnnotation(AutoFill.class)：表示获取该方法上标记的 @AutoFill 注解对象。
//        利用反射获取方法上的注解实例
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

//        2. 获取当前被拦截的方法的参数-实体对象
//        get all the given arguments
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0){
            return;
        }
//        后期可能传进来菜品/用户实体，用object来接接受
        Object entity = args[0];

//        3. 准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

//        4. 根据当前不同的操作类型，为对应的属性通过反射来赋值
        if(operationType == OperationType.INSERT){
//    为4个公共字段赋值
            try {
//  entity.getClass() 获取运行时对象 entity 的 Class 对象（类的结构信息）。
//  .getDeclaredMethod(...)：获取这个类中名为**的方法，第一个参数是方法名，第二个方法名是参数类型
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

//通过反射为对象属性赋值，相当于执行entity.setCreateTime(now);
                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }else if(operationType == OperationType.UPDATE){
            //    为4个公共字段赋值
            try {

                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

//                通过反射为对象属性赋值

                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

    }


}}
