package com.petcare.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.common.Result;
import com.petcare.system.entity.User;
import com.petcare.system.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserMapper userMapper;

    @GetMapping("/list")
    public ResponseEntity<Result<Page<User>>> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(User::getPhone, keyword)
                    .or()
                    .like(User::getNickname, keyword)
                    .or()
                    .like(User::getRealName, keyword));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userMapper.selectPage(new Page<>(page, size), wrapper);
        // 脱敏：清除密码字段
        result.getRecords().forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(Result.success(result));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Result<Void>> toggleStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        User user = userMapper.selectById(id);
        if (user == null) {
            return ResponseEntity.ok(Result.error(404, "用户不存在"));
        }
        user.setStatus(status);
        userMapper.updateById(user);
        return ResponseEntity.ok(Result.success(status == 1 ? "已启用" : "已禁用", null));
    }
}
