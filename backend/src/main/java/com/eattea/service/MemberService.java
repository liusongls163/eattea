package com.eattea.service;

import com.eattea.entity.Member;
import com.eattea.mapper.MemberMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    private final MemberMapper memberMapper;

    public MemberService(MemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    public List<Member> listAll() {
        return memberMapper.selectAll();
    }

    public Member getMember(Long id) {
        return memberMapper.selectById(id);
    }

    public Member create(Member member) {
        memberMapper.insert(member);
        return member;
    }

    public void update(Member member) {
        memberMapper.update(member);
    }

    public void delete(Long id) {
        memberMapper.deleteById(id);
    }
}
