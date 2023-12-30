/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.enviro.assessment.grad001.thatokujwane;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Thato Keith Kujwane
 */
@Repository
public interface InvestorRepository extends JpaRepository<Investor, Long> {
    
}
