package pl.dorzak.transferservice.account

import com.typesafe.config.ConfigFactory
import org.jdbi.v3.core.Handle
import pl.dorzak.transferservice.config.JdbiFactory
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import static org.hamcrest.Matchers.containsInAnyOrder
import static spock.util.matcher.HamcrestSupport.that

class AccountTransactionLegDaoSpec extends Specification {

    private static final def TESTED_ACCOUNT_ID = UUID.fromString('d6896820-7f3e-11e9-b475-0800200c9a66')
    @Shared
    @AutoCleanup
    private Handle handle
    @Shared
    private AccountTransactionLegDao dao

    def setupSpec() {
        def jdbi = new JdbiFactory().create(ConfigFactory.load())
        handle = jdbi.open()
        dao = handle.attach(AccountTransactionLegDao.class)
    }

    def "insertAccountTransactionLeg should create row in database"() {
        given:
        def currency = Currency.getInstance('GBP')
        def value = BigDecimal.valueOf(-100.0)

        when:
        def legId = dao.insertAccountTransactionLeg(TESTED_ACCOUNT_ID, currency, value)

        then:
        dao.findById(legId).isPresent()
        and:
        dao.findByAccountId(TESTED_ACCOUNT_ID).size() == 1

        cleanup:
        dao.deleteById(legId)
    }

    def "findByAccountId should all transaction legs of account"() {
        given:
        def legId1 = dao.insertAccountTransactionLeg(TESTED_ACCOUNT_ID, Currency.getInstance('GBP'), BigDecimal.valueOf(100.0))
        def legId2 = dao.insertAccountTransactionLeg(TESTED_ACCOUNT_ID, Currency.getInstance('GBP'), BigDecimal.valueOf(-50.43))
        def legId3 = dao.insertAccountTransactionLeg(TESTED_ACCOUNT_ID, Currency.getInstance('GBP'), BigDecimal.valueOf(80.0))

        when:
        def queryResult = dao.findByAccountId(TESTED_ACCOUNT_ID)

        then:
        queryResult.size() == 3
        and:
        that queryResult.collect { it.id }, containsInAnyOrder(legId1, legId2, legId3)

        cleanup:
        queryResult.each { dao.deleteById(it.id) }
    }
}

