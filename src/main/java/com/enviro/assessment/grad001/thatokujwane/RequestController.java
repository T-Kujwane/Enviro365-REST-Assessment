/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.enviro.assessment.grad001.thatokujwane;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author Thato Keith Kujwane
 */
@Controller
public class RequestController {
    @RequestMapping("/addInvestor")
    public String addInvestor(){
        return "investorAdd.html";
    }
    
    @RequestMapping("/addInvestment")
    public String addInvestment(){
        return "addInvestment.html";
    }
    
    @RequestMapping("/getInvestorProducts")
    public String getInvestorProducts(){
        return "getInvestorProducts.html";
    }
    
    @RequestMapping("/fileWithdrawalNotice")
    public String withdraw(){
        return "withdraw.html";
    }
    
}
