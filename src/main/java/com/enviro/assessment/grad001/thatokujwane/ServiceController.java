/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.enviro.assessment.grad001.thatokujwane;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

/**
 *
 * @author Thato Keith Kujwane
 */
@RestController
public class ServiceController {

    @Autowired
    private InvestorRepository investorRepository;

    private String getParameter(String param, HttpServletRequest request) {
        return request.getParameter(param);
    }

    private Investment getInvestment(HttpServletRequest request) {
        String chosenInvestmentType = getParameter("investmentType", request);
        Double initialInvestmentAmount = Double.valueOf(getParameter("investmentAmount", request));

        Product investmentProduct = new Product();
        investmentProduct.setType(chosenInvestmentType);

        Investment investment = new Investment();
        investment.setCurrentBalance(initialInvestmentAmount);
        investment.setInitialDepositAmount(initialInvestmentAmount);
        investment.setProduct(investmentProduct);

        return investment;
    }

    @PostMapping("/addNewInvestor")
    public RedirectView add(HttpServletRequest request) {
        String firstName = getParameter("firstName", request);
        String lastName = getParameter("lastName", request);
        String phoneNumber = getParameter("phoneNumber", request);
        String emailAddress = getParameter("emailAddress", request);

        ContactDetails details = new ContactDetails();
        details.setEmailAddress(emailAddress);
        details.setPhoneNumber(phoneNumber);

        String street = getParameter("street", request);
        String suburb = getParameter("suburb", request);
        String city = getParameter("city", request);
        String postalCode = getParameter("postalCode", request);

        Address investorAddress = new Address();
        investorAddress.setStreet(street);

        if (!suburb.equalsIgnoreCase("null")) {
            investorAddress.setSuburb(suburb);
        }

        investorAddress.setCity(city);
        investorAddress.setPostalCode(postalCode);

        Investment investment = getInvestment(request);

        Investor newInvestor = Investor.builder()
                .address(investorAddress)
                .investment(investment)
                .contactDetails(details)
                .firstName(firstName)
                .lastName(lastName)
                .idNumber(getParameter("idNumber", request))
                .build();

        this.investorRepository.save(newInvestor);

        return new RedirectView("/index.html");
    }

    @PostMapping("/addClientInvestment")
    public String addClientInvestment(@RequestParam("idNumber") String idNumber,
            @RequestParam("investmentType") String investmentType, @RequestParam("investmentAmount") String investmentAmount) {
        Investor investor = getInvestor(idNumber);

        if (investor != null) {
            double depositAmount = Double.parseDouble(investmentAmount);
            investor.getInvestmentsList().add(
                    Investment.builder().
                            initialDepositAmount(depositAmount).
                            currentBalance(depositAmount).
                            product(Product.builder().type(investmentType).build()).
                            build()
            );
            this.investorRepository.save(investor);
            return "Investment successfuly added to investment profile";

        }

        return "Investor with ID " + idNumber + " was not found";
    }

    @GetMapping("/getInvestorDetails")
    @ResponseBody
    public String getInvestorDetails(@RequestParam("idNumber") String idNumber) throws JsonProcessingException {

        Investor investor = getInvestor(idNumber);
        if (investor != null) {
            //modelView = new ModelAndView("investorDisplay.html", HttpStatus.OK);
            Map<String, String> detailsMap = new HashMap<>();
            addObject(detailsMap, "idNumber", idNumber);
            addObject(detailsMap, "firstName", investor.getFirstName());
            addObject(detailsMap, "lastName", investor.getLastName());
            addObject(detailsMap, "emailAddress", investor.getContactDetails().getEmailAddress());
            addObject(detailsMap, "phoneNumber", investor.getContactDetails().getPhoneNumber());
            addObject(detailsMap, "street", investor.getAddress().getStreet());
            addObject(detailsMap, "surbub", investor.getAddress().getSuburb());
            addObject(detailsMap, "city", investor.getAddress().getCity());
            addObject(detailsMap, "postalCode", investor.getAddress().getPostalCode());

            return new ObjectMapper().writeValueAsString(detailsMap);
        }

        return "Investor with ID " + idNumber + " was not found.";
    }

    private Investor getInvestor(String idNumber) {
        List<Investor> investorsList = this.investorRepository.findAll();

        for (Investor investor : investorsList) {
            if (investor.getIdNumber().equals(idNumber)) {
                return investor;
            }
        }

        return null;
    }

    private void addObject(Map<String, String> map, String key, String val) {
        map.put(key, val);
    }

    @GetMapping("/getInvestorProducts")
    @ResponseBody
    public String getInvestorProducts(@RequestParam("idNumber") String idNumber) throws JsonProcessingException {
        Investor investor = getInvestor(idNumber);

        return investor != null
                ? new ObjectMapper().writeValueAsString(investor.getInvestmentsList())
                : "Investor with ID " + idNumber + " was not found.";
    }

    @PostMapping("/withdraw")
    @ResponseBody
    public String withdraw(@RequestParam("idNumber") String idNumber,
            @RequestParam("investmentId") Long investmentId,
            @RequestParam("withdrawalAmount") String amount,
            @RequestParam("clientAge") Integer clientAge) throws JsonProcessingException {
        Investor investor = this.getInvestor(idNumber);
        String processingOutcome = "Investor with ID " + idNumber + " was not found";

        if (investor != null) {
            if (clientAge != 0) {
                for (Investment clientInvestment : investor.getInvestmentsList()) {
                    if (clientInvestment.getId() == investmentId) {
                        double investmentBalance = clientInvestment.getCurrentBalance();
                        double withdrawalAmount = Double.parseDouble(amount);
                        if (withdrawalAmount <= (0.95 * investmentBalance)) {
                            if (withdrawalAmount >= investmentBalance) {
                                processingOutcome = "Withdrawal failed. Insufficient funds.";
                            } else {
                                if ((clientInvestment.getProduct().getType().equalsIgnoreCase("retirement") && clientAge >= 65) || 
                                        clientInvestment.getProduct().getType().equalsIgnoreCase("savings")) {
                                    investor.getInvestmentsList().get(
                                            investor.getInvestmentsList().indexOf(clientInvestment)
                                    ).setCurrentBalance(investmentBalance - withdrawalAmount);
                                    this.investorRepository.saveAndFlush(investor);
                                    processingOutcome = "Withdrawal sucessful";
                                    break;
                                }else {
                                    processingOutcome = "Withdrawal failed due to client not meeting age requirement for retirement withdrawal";
                                }
                            }
                        } else {
                            processingOutcome = "Withdrawal failed. Cannot withdraw more than 90% of the current balance";
                        }
                    }
                }
            } else {
                processingOutcome = "Invalid age given";
            }

        }

        return new ObjectMapper().writeValueAsString(processingOutcome);
    }
}
