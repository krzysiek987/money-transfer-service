package pl.dorzak.transferservice.account

import com.typesafe.config.ConfigFactory
import org.jdbi.v3.core.Handle
import pl.dorzak.transferservice.config.JdbiFactory
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class AccountDaoSpec extends Specification {

    @Shared
    @AutoCleanup
    private Handle handle
    @Shared
    private AccountDao dao

    def setupSpec() {
        def jdbi = new JdbiFactory().create(ConfigFactory.load())
        handle = jdbi.open()
        handle.attach(AccountDao.class)

        dao = handle.attach(AccountDao.class)
    }

    def "insert should create account"() {
        given:
        def id = UUID.fromString('191765d0-06be-4e01-980b-455688698a6b')
        def currency = Currency.getInstance('GBP')

        expect:
        !dao.findById(id).isPresent()

        when:
        dao.insertAccount(id, currency)

        then:
        dao.findById(id).isPresent()

        cleanup:
        dao.deleteById(id)
    }

    def "findById should return proper account"() {
        given:
        def accountId = UUID.fromString('d6896820-7f3e-11e9-b475-0800200c9a66')

        when:
        def transfer = dao.findById(accountId)

        then:
        transfer.isPresent()
        and:
        transfer.get().currency == Currency.getInstance('PLN')
    }

    def "should delete account"() {
        given:
        def accountId = UUID.fromString('d6896820-7f3e-11e9-b475-0800200c9a66')
        def existingAccount = dao.findById(accountId)

        expect:
        existingAccount.isPresent()

        when:
        dao.deleteById(accountId)

        then:
        !dao.findById(accountId).isPresent()

        cleanup:
        def toRestore = existingAccount.get()
        dao.insertAccount(toRestore.id, toRestore.currency)
    }

}
