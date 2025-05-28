package com.example.SanChoi247.config;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.example.SanChoi247.model.entity.User;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        String uri = httpServletRequest.getRequestURI();
        HttpSession session = (HttpSession) httpServletRequest.getSession();
        User user = (User) session.getAttribute("UserAfterLogin");
        
        // AP DUNG CHO TAT CA CAC ROLE
        if ((uri.startsWith("/api/messages") || uri.equals("/UserAfterLogin") || uri.equals("/Login") || uri.equals("/") || uri.equals("/LoginToSystem") || uri.equals("/ShowForOwners") || uri.equals("/ShowIntroduction") || uri.startsWith("/ViewDetail/")
                || uri.startsWith("/ShowDetailLocation/") || uri.equals("/Logout") || uri.equals("/Signup") || uri.equals("/VerifyEmail") || uri.equals("/SearchSanByTenSan") || uri.equals("/ShowSearch") || uri.equals("/resendOtp") || uri.equals("/auth/enterOtp") || uri.equals("/verifyOtp") || uri.equals("forgot-password") || uri.equals("verify-otp") || uri.equals("/reset-password"))) {
            chain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        // AP DUNG CHO ROLE ADMIN
        if (user == null) {
            httpServletResponse.sendRedirect("/Login");
        } else if (user.getRole() == 'C' && uri.equals("/ShowEditProfile") || uri.startsWith("/EditProfile") || uri.equals("/ShowChangePassword") || uri.startsWith("/changePassword") || uri.equals("/user/requestFieldOwner") || uri.equals("/owner/registerField") || uri.startsWith("/editBooking/") || uri.equals("/editBooking") || uri.equals("/showAddScheduleBooking") || uri.equals("/addScheduleBooking") || uri.equals("/update-password") || uri.equals("owner/dashboard") || uri.equals("/ViewOwnerRefundRequests") || uri.equals("/ViewUserBooking") || uri.equals("/ViewTotalRefund") || uri.equals("/sendMailToViewBookingOwner") || uri.equals("/sendMailToViewAllRequest") || uri.equals("/ApproveRequestOwner") || uri.equals("/RejectRequestOwner") || uri.equals("/owner/addSmallField") || uri.equals("/all") || uri.equals("/blog") || uri.equals("/posts") || uri.equals("/posts/delete") || uri.equals("/posts/edit") || uri.equals("/posts/update") || uri.equals("/comments") || uri.equals("/comments/edit") || uri.equals("/comments/delete") || uri.startsWith("/upload")) {// AP DUNG CHO owner
            chain.doFilter(httpServletRequest, httpServletResponse);
        } else if (user.getRole() == 'C' && uri.equals("/ShowEditProfile") || uri.startsWith("/EditProfile") || uri.equals("/ShowChangePassword") || uri.startsWith("/changePassword") || uri.equals("/update-password") | uri.equals("/all") || uri.equals("/blog") || uri.equals("/posts") || uri.equals("/posts/delete") || uri.equals("/posts/edit") || uri.equals("/posts/update") || uri.equals("/comments") || uri.equals("/comments/edit") || uri.equals("/comments/delete") || uri.startsWith("/upload") || uri.equals("/bookSan") || uri.equals("/vnpay_return") || uri.equals("/ShowBookingByUserId") || uri.equals("/requestRefund") || uri.equals("/rate") || uri.equals("/index")) {
            chain.doFilter(httpServletRequest, httpServletResponse);
        } else if (user.getRole() == 'A') {
            chain.doFilter(httpServletRequest, httpServletResponse);
        }

        else {
            httpServletResponse.sendRedirect("/");
        }
    }
}
