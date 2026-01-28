package com.heritage.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heritage.entity.User;
import com.heritage.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getAccount, account);
        User user = userMapper.selectOne(wrapper);
        
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + account);
        }

        return new CustomUserDetails(
                user.getAccount(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole())),
                user.getId()
        );
    }
}
