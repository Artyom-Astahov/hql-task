package by.itacademy.hibernate.dao;


import by.itacademy.hibernate.dto.PaymentFilter;
import by.itacademy.hibernate.dto.QPredicate;
import by.itacademy.hibernate.dto.UserFilter;
import by.itacademy.hibernate.entity.*;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.Session;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static by.itacademy.hibernate.entity.QChat.*;
import static by.itacademy.hibernate.entity.QCompany.company;
import static by.itacademy.hibernate.entity.QPayment.payment;
import static by.itacademy.hibernate.entity.QUser.user;
import static by.itacademy.hibernate.entity.QUserChat.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserDao {

    private static final UserDao INSTANCE = new UserDao();

    /**
     * Возвращает всех сотрудников
     */
    public List<User> findAll(Session session) {


        return new JPAQuery<User>(session).select(user).from(user).fetch();
    }

    /**
     * Возвращает всех сотрудников с указанным именем
     */
    public List<User> findAllByFirstName(Session session, String firstName) {
        return new JPAQuery<User>(session).select(user).from(user)
                .where(user.personalInfo().firstname.eq(firstName)).fetch();
    }

    /**
     * Возвращает первые {limit} сотрудников, упорядоченных по дате рождения (в порядке возрастания)
     */
    public List<User> findLimitedUsersOrderedByBirthday(Session session, int limit) {

        return new JPAQuery<User>(session).select(user).from(user)
                .orderBy(new OrderSpecifier(Order.ASC, user.personalInfo().birthDate))
                .limit(limit).fetch();
    }

    /**
     * Возвращает всех сотрудников компании с указанным названием
     */
    public List<User> findAllByCompanyName(Session session, String companyName) {

        return new JPAQuery<User>(session).select(user).from(company)
                .join(company.users, user)
                .where(company.name.eq(companyName)).fetch();
    }

    /**
     * Возвращает все выплаты, полученные сотрудниками компании с указанными именем,
     * упорядоченные по имени сотрудника, а затем по размеру выплаты
     */
    public List<Payment> findAllPaymentsByCompanyName(Session session, String companyName) {

        return new JPAQuery<Payment>(session)
                .select(payment)
                .from(company)
                .join(company.users, user)
                .join(user.payments, payment)
                .where(company.name.eq(companyName))
                .orderBy(user.personalInfo().firstname.asc(), payment.amount.asc())
                .fetch()
                ;
    }

    /**
     * Возвращает среднюю зарплату сотрудника с указанными именем и фамилией
     */
    public Double findAveragePaymentAmountByFirstAndLastNames(Session session, PaymentFilter filter) {

        /*List<Predicate> predicates = new ArrayList<>();
        if (filter.getFirstName() != null)
            predicates.add(user.personalInfo().firstname.eq(filter.getFirstName()));
        if (filter.getLastname() != null)
            predicates.add(user.personalInfo().lastname.eq(filter.getLastname()));*/

        var predicate = QPredicate.builder()
                .add(filter.getFirstName(), user.personalInfo().firstname::eq)
                .add(filter.getLastname(), user.personalInfo().lastname::eq)
                .buildAnd();

        return new JPAQuery<Double>(session)
                .select(payment.amount.avg())
                .from(user)
                .join(user.payments, payment)
                .where(predicate)
                .fetchOne();
    }

    /**
     * Возвращает для каждой компании: название, среднюю зарплату всех её сотрудников. Компании упорядочены по названию.
     */
    public List<Tuple> findCompanyNamesWithAvgUserPaymentsOrderedByCompanyName(Session session) {

        return new JPAQuery<Tuple>(session)
                .select(company.name, payment.amount.avg())
                .from(company)
                .join(company.users, user)
                .join(user.payments, payment)
                .groupBy(company.name)
                .orderBy(payment.amount.avg().asc())
                .fetch();

    }

    /**
     * Возвращает список: сотрудник (объект User), средний размер выплат, но только для тех сотрудников, чей средний размер выплат
     * больше среднего размера выплат всех сотрудников
     * Упорядочить по имени сотрудника
     */
    public List<Tuple> isItPossible(Session session) {

        return new JPAQuery<Tuple>(session)
                .select(user, payment.amount.avg())
                .from(user)
                .join(user.payments, payment)
                .groupBy(user.id)
                .having(payment.amount.avg().gt(
                        new JPAQuery<Double>(session)
                                .select(payment.amount.avg())
                                .from(payment)
                ))
                .orderBy(user.personalInfo().firstname.asc())
                .fetch();
    }

    /**
     * Вывести все чаты, в которых участвует определенный пользователь.
     */
    public List<Tuple> findAllUserChats(Session session, UserFilter filter) {

        var predicate = QPredicate.builder()
                .add(filter.getFirstName(), user.personalInfo().firstname::eq)
                .add(filter.getLastName(), user.personalInfo().lastname::eq)
                .buildAnd();

        return new JPAQuery<Tuple>(session)
                .select(user.username, chat.name)
                .from(userChat)
                .join(userChat.user(), user)
                .join(userChat.chat(), chat)
                .where(predicate)
                .groupBy(user, chat)
                .fetch();

    }
    /**
     * Выберите пользователей, у которых в имени пользователя содержится {str}
     */
    public List<User> findUserByLine(Session session, String str){

        return new JPAQuery<User>(session)
                .select(user)
                .from(user)
                .where(user.username.contains(str))
                .fetch();
    }

    /**
     * Возвращает пользователей, которые живут на улице {street}
     * */

    public List<User> findUsersByStreet(Session session, String street){
        return new JPAQuery<User>(session)
                .select(user)
                .from(user)
                .where(user.profile().street.eq(street))
                .fetch();
    }

    /**
     * Возвращает всех админов
     * */
    public List<User> findAdmin(Session session){
        return new JPAQuery<User>(session)
                .select(user)
                .from(user)
                .where(user.role.eq(Role.ADMIN))
                .fetch();
    }

    /**
     * Возвращает всех юзеров
     * */
    public List<User> findUser(Session session){
        return new JPAQuery<User>(session)
                .select(user)
                .from(user)
                .where(user.role.eq(Role.USER))
                .fetch();
    }

    public static UserDao getInstance() {
        return INSTANCE;
    }
}