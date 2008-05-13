package org.springframework.webflow.samples.booking;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;

/**
 * A Hotel Booking made by a User.
 */
@Entity
public class Booking implements Serializable {

    private static final long serialVersionUID = 1171567558348174963L;

    private Long id;

    private User user;

    private Hotel hotel;

    private Date checkinDate;

    private Date checkoutDate;

    private String creditCard;

    private String creditCardName;

    private int creditCardExpiryMonth;

    private int creditCardExpiryYear;

    private boolean smoking;

    private int beds;

    public Booking() {
    }

    public Booking(Hotel hotel, User user) {
	this.hotel = hotel;
	this.user = user;
	Calendar calendar = Calendar.getInstance();
	setCheckinDate(calendar.getTime());
	calendar.add(Calendar.DAY_OF_MONTH, 1);
	setCheckoutDate(calendar.getTime());
    }

    @Transient
    public BigDecimal getTotal() {
	return hotel.getPrice().multiply(new BigDecimal(getNights()));
    }

    @Transient
    public int getNights() {
	if (checkinDate == null || checkoutDate == null) {
	    return 0;
	} else {
	    return (int) (checkoutDate.getTime() - checkinDate.getTime()) / 1000 / 60 / 60 / 24;
	}
    }

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    public Long getId() {
	return id;
    }

    public void setId(Long id) {
	this.id = id;
    }

    @Basic
    @Temporal(TemporalType.DATE)
    public Date getCheckinDate() {
	return checkinDate;
    }

    public void setCheckinDate(Date datetime) {
	this.checkinDate = datetime;
    }

    @ManyToOne
    public Hotel getHotel() {
	return hotel;
    }

    public void setHotel(Hotel hotel) {
	this.hotel = hotel;
    }

    @ManyToOne
    public User getUser() {
	return user;
    }

    public void setUser(User user) {
	this.user = user;
    }

    @Basic
    @Temporal(TemporalType.DATE)
    public Date getCheckoutDate() {
	return checkoutDate;
    }

    public void setCheckoutDate(Date checkoutDate) {
	this.checkoutDate = checkoutDate;
    }

    public String getCreditCard() {
	return creditCard;
    }

    public void setCreditCard(String creditCard) {
	this.creditCard = creditCard;
    }

    @Transient
    public String getDescription() {
	DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
	return hotel == null ? null : hotel.getName() + ", " + df.format(getCheckinDate()) + " to "
		+ df.format(getCheckoutDate());
    }

    public boolean isSmoking() {
	return smoking;
    }

    public void setSmoking(boolean smoking) {
	this.smoking = smoking;
    }

    public int getBeds() {
	return beds;
    }

    public void setBeds(int beds) {
	this.beds = beds;
    }

    public String getCreditCardName() {
	return creditCardName;
    }

    public void setCreditCardName(String creditCardName) {
	this.creditCardName = creditCardName;
    }

    public int getCreditCardExpiryMonth() {
	return creditCardExpiryMonth;
    }

    public void setCreditCardExpiryMonth(int creditCardExpiryMonth) {
	this.creditCardExpiryMonth = creditCardExpiryMonth;
    }

    public int getCreditCardExpiryYear() {
	return creditCardExpiryYear;
    }

    public void setCreditCardExpiryYear(int creditCardExpiryYear) {
	this.creditCardExpiryYear = creditCardExpiryYear;
    }

    public void validateEnterBookingDetails(MessageContext context) {
	if (checkinDate.before(today())) {
	    context.addMessage(new MessageBuilder().error().source("checkinDate").defaultText(
		    "The Check In Date must be a future date").build());
	} else if (!checkinDate.before(checkoutDate)) {
	    context.addMessage(new MessageBuilder().error().source("checkoutDate").defaultText(
		    "The Check Out Date must be later than the Check In Date").build());
	}
    }

    private Date today() {
	Calendar calendar = Calendar.getInstance();
	calendar.add(Calendar.DAY_OF_MONTH, -1);
	return calendar.getTime();
    }

    @Override
    public String toString() {
	return "Booking(" + user + "," + hotel + ")";
    }

}
