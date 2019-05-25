package pl.dorzak.transferservice.transfer

import com.typesafe.config.ConfigFactory
import pl.dorzak.transferservice.config.DataSourceFactory
import spock.lang.Specification

import java.time.Instant

class TransferRepositoryTest extends Specification {

    private TransferRepository transferRepository


    def setup() {
        def dataSource = new DataSourceFactory().createDataSource(ConfigFactory.load())
        this.transferRepository = new TransferRepository(dataSource)
    }

    def "findByTransactionTypeAndSourceAccountId should return filtered transfers"() {
        given:
        def accountId = UUID.fromString('b0fa49a2-54d7-4a06-a569-f40f082f8dd7')
        def expectedTransferIds = ['31a17d04-741f-4b3d-af78-e5be263743a1', '400a020a-b8f4-467c-8337-763ec892aeee', 'beeaedce-12b8-4739-8904-bb5c53acc32c']
        when:
        def transfers = transferRepository.findBySourceAccountId(accountId)
        then:
        transfers.size() == 3
        and:
        transfers.collect { it.id.toString() } == expectedTransferIds

    }

    def "findById should return transfer with proper id"() {
        given:
        def transferId = UUID.fromString('400a020a-b8f4-467c-8337-763ec892aeee')
        when:
        def transfer = transferRepository.findById(transferId)
        then:
        transfer != null
        and:
        transfer.amount == BigDecimal.valueOf(100.0)
    }

    def "save should insert transfer when it was missing"() {
        given:
        def transferToInsert = Transfer.builder()
                .status(TransferStatus.PENDING)
                .sourceAccountId(UUID.fromString('b8cca210-eaa4-4a7b-91d6-36195a2f2106'))
                .destinationAccountId(UUID.fromString('42aa7c50-05da-4a13-952c-ebc8cb12dbac'))
                .amount(BigDecimal.valueOf(1.10))
                .currency(Currency.getInstance('EUR'))
                .createdAt(Instant.ofEpochMilli(1558808555000L))
                .build()

        when:
        def insertedTransfer = transferRepository.save(transferToInsert)

        then:
        insertedTransfer != null
        and:
        transferRepository.findById(insertedTransfer.getId()) == insertedTransfer

        cleanup:
        transferRepository.deleteById(insertedTransfer.getId())
    }

    def "save should update transfer when it was present"() {
        given:
        def transferId = UUID.fromString('beeaedce-12b8-4739-8904-bb5c53acc32c')
        def existingTransfer = transferRepository.findById(transferId)
        def updateRequest = existingTransfer.toBuilder()
                .amount(BigDecimal.valueOf(150.55))
                .status(TransferStatus.FINISHED)
                .build()

        when:
        def updatedTransfer = transferRepository.save(updateRequest)

        then:
        updateRequest == updatedTransfer
        and:
        transferRepository.findById(transferId) == updatedTransfer

        cleanup:
        transferRepository.save(existingTransfer)
    }

    def "deleteById "() {
        given:
        def transferId = UUID.fromString('1d0f8741-aeb8-4dd6-9faf-f7289636a042')
        def existingTransfer = transferRepository.findById(transferId)

        expect:
        existingTransfer != null

        when:
        transferRepository.deleteById(transferId)

        then:
        transferRepository.findById(transferId) == null

        cleanup:
        transferRepository.save(existingTransfer)
    }
}
