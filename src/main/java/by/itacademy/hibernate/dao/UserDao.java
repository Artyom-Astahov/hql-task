package by.itacademy.hibernate.dao;


import by.itacademy.hibernate.entity.Payment;
import by.itacademy.hibernate.entity.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.Session;

import java.util.Collections;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserDao {

    private static final UserDao INSTANCE = new UserDao();

    /**
     * Возвращает всех сотрудников
     */
    public List<User> findAll(Session session) {
        List<User> users = session.createQuery("select u from User u", User.class).list();
        return users;
    }

    /**
     * Возвращает всех сотрудников с указанным именем
     */
    public List<User> findAllByFirstName(Session session, String firstName) {

        List<User> users = session.createQuery("""
                        select u 
                        from User u 
                        where u.personalInfo.firstname = :firstName""", User.class)
                .setParameter("firstName", firstName)
                .list();

        return users;
    }

    /**
     * Возвращает первые {limit} сотрудников, упорядоченных по дате рождения (в порядке возрастания)
     */
    public List<User> findLimitedUsersOrderedByBirthday(Session session, int limit) {

        List<User> users = session.createQuery("""
                        select u 
                        from User u
                        order by u.personalInfo.birthDate
                        """, User.class)
                .setMaxResults(limit)
                .list();
        return users;
    }

    /**
     * Возвращает всех сотрудников компании с указанным названием
     */
    public List<User> findAllByCompanyName(Session session, String companyName) {
        List<User> users = session.createQuery("""
                            select u
                            from User u
                            left join u.company c 
                            where c.name = :companyName
                        """, User.class)
                .setParameter("companyName", companyName)
                .list();
        return users;
    }

    /**
     * Возвращает все выплаты, полученные сотрудниками компании с указанными именем,
     * упорядоченные по имени сотрудника, а затем по размеру выплаты
     */
    public List<Payment> findAllPaymentsByCompanyName(Session session, String companyName) {
        List<Payment> payments = session.createQuery("""
                                select p
                                from User u
                                join u.company c 
                                join u.payments p 
                                where c.name = :companyName
                                order by u.personalInfo.firstname, p.amount
                        """, Payment.class)
                .setParameter("companyName", companyName)
                .list();
        return payments;
    }

    /**
     * Возвращает среднюю зарплату сотрудника с указанными именем и фамилией
     */
    public Double findAveragePaymentAmountByFirstAndLastNames(Session session, String firstName, String lastName) {

        List<Double> avg = session.createQuery("""
                         select avg(p.amount) 
                         from User u
                         join u.payments p 
                         where u.personalInfo.firstname = :firstName and u.personalInfo.lastname = :lastName
                        """, Double.class)
                .setParameter("firstName", firstName)
                .setParameter("lastName", lastName)
                .list();

        return avg.get(0);
    }

    /**
     * Возвращает для каждой компании: название, среднюю зарплату всех её сотрудников. Компании упорядочены по названию.
     */
    public List<Object[]> findCompanyNamesWithAvgUserPaymentsOrderedByCompanyName(Session session) {

        var obj = session.createQuery("""
                select c.name, avg(p.amount)
                from User u
                right join u.company c
                right join u.payments p  
                group by c.name
                order by c.name
                """).list();
        return obj;
    }

    /**
     * Возвращает список: сотрудник (объект User), средний размер выплат, но только для тех сотрудников, чей средний размер выплат
     * больше среднего размера выплат всех сотрудников
     * Упорядочить по имени сотрудника
     */
    public List<Object[]> isItPossible(Session session) {

        var payout = session.createQuery("""
                select avg(p.amount)
                from Payment p
                """).list();

        var user = session.createQuery("""
                select u, avg(p.amount)
                from User u
                right join u.payments p 
                group by u
                having avg(p.amount) > :payout
                order by u.personalInfo.firstname
                """)
                .setParameter("payout", payout.get(0))
                .list();
        return user;
    }

    public static UserDao getInstance() {
        return INSTANCE;
    }
}