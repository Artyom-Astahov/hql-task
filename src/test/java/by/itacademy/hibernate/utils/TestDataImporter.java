package by.itacademy.hibernate.utils;


import by.itacademy.hibernate.entity.*;
import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.persistence.Enumerated;
import java.time.LocalDate;
import java.time.Month;

@UtilityClass
public class TestDataImporter {

    public void importData(SessionFactory sessionFactory) {
        @Cleanup Session session = sessionFactory.openSession();

        Company microsoft = saveCompany(session, "Microsoft");
        Company apple = saveCompany(session, "Apple");
        Company google = saveCompany(session, "Google");

        User billGates = saveUser(session, "Bill", "Gates",
                LocalDate.of(1955, Month.OCTOBER, 28), microsoft, Role.USER);
        User steveJobs = saveUser(session, "Steve", "Jobs",
                LocalDate.of(1955, Month.FEBRUARY, 24), apple, Role.ADMIN);
        User sergeyBrin = saveUser(session, "Sergey", "Brin",
                LocalDate.of(1973, Month.AUGUST, 21), google, Role.USER);
        User timCook = saveUser(session, "Tim", "Cook",
                LocalDate.of(1960, Month.NOVEMBER, 1), apple, Role.ADMIN);
        User dianeGreene = saveUser(session, "Diane", "Greene",
                LocalDate.of(1955, Month.JANUARY, 1), google, Role.USER);

        Chat telegramTimCook = saveChat(session, "Working chat in telegram");

        addUserSetChat(session, timCook, telegramTimCook);

        Profile profileTimCook = saveProfile(session, timCook, Language.JAVA, "1600 Pennsylvania Ave.");
        Profile profileBillGates = saveProfile(session, billGates, Language.GO, "10 Downing Street");
        Profile profileSteveJobs = saveProfile(session, steveJobs, Language.KOTLIN, "500 South Great Room Trail");
        Profile profileSergeyBrin = saveProfile(session, sergeyBrin, Language.PYTHON, "123 Main Street");
        Profile profileDianeGreene = saveProfile(session, dianeGreene, Language.JAVA, "1600 Pennsylvania Ave.");


        savePayment(session, billGates, 100);
        savePayment(session, billGates, 300);
        savePayment(session, billGates, 500);

        savePayment(session, steveJobs, 250);
        savePayment(session, steveJobs, 600);
        savePayment(session, steveJobs, 500);

        savePayment(session, timCook, 400);
        savePayment(session, timCook, 300);

        savePayment(session, sergeyBrin, 500);
        savePayment(session, sergeyBrin, 500);
        savePayment(session, sergeyBrin, 500);

        savePayment(session, dianeGreene, 300);
        savePayment(session, dianeGreene, 300);
        savePayment(session, dianeGreene, 300);
    }

    private Company saveCompany(Session session, String name) {
        Company company = Company.builder()
                .name(name)
                .build();
        session.save(company);

        return company;
    }

    private Profile saveProfile(Session session,
                                User user,
                                Enum<Language> language,
                                String street) {
        Profile profile = Profile.builder()
                .street(street)
                .language(language.toString())
                .build();
        profile.setUser(user);
        session.save(profile);
        return profile;
    }

    private User saveUser(Session session,
                          String firstName,
                          String lastName,
                          LocalDate birthday,
                          Company company,
                          Role role) {
        User user = User.builder()
                .username(firstName + lastName)
                .personalInfo(PersonalInfo.builder()
                        .firstname(firstName)
                        .lastname(lastName)
                        .birthDate(new Birthday(birthday))
                        .build())
                .role(role)
                .company(company)
                .build();
        session.save(user);

        return user;
    }

    private void savePayment(Session session, User user, Integer amount) {
        Payment payment = Payment.builder()
                .receiver(user)
                .amount(amount)
                .build();
        session.save(payment);
    }

    private Chat saveChat(Session session, String name) {
        Chat chat = Chat.builder()
                .name(name)
                .build();
        session.save(chat);
        return chat;
    }

    private void addUserSetChat(Session session, User user, Chat chat) {
        UserChat userChat = UserChat.builder().build();
        userChat.setChat(chat);
        userChat.setUser(user);
        session.save(userChat);

    }
}