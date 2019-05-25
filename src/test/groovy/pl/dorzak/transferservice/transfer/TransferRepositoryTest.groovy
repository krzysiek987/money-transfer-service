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

    def "findBySourceAccountId should return filtered transfers"() {
        given:
        def accountId = UUID.fromString('64daa53d-10c9-4f1b-92c4-63c543823da6')
        def expectedTransferIds = ['31a17d04-741f-4b3d-af78-e5be263743a1', '400a020a-b8f4-467c-8337-763ec892aeee', 'beeaedce-12b8-4739-8904-bb5c53acc32c']
        when:
        def transfers = transferRepository.findBySourceAccountId(accountId)
        then:
        transfers.size() == 3
        and:
        transfers.collect { it.id.toString() } == expectedTransferIds

    }

    def "findById should return transfer with proper amount"() {
        given:
        def transferId = UUID.fromString('400a020a-b8f4-467c-8337-763ec892aeee')
        when:
        def transfer = transferRepository.findById(transferId)
        then:
        transfer.isPresent()
        and:
        transfer.get().amount == BigDecimal.valueOf(100.0)
    }

    def "should insert transfer and generate it's id"() {
        given:
        def transferToInsert = Transfer.builder()
                .status(TransferStatus.PENDING)
                .sourceAccountId(UUID.fromString('b8cca210-eaa4-4a7b-91d6-36195a2f2106'))
                .destinationAccountId(UUID.fromString('42aa7c50-05da-4a13-952c-ebc8cb12dbac'))
                .amount(BigDecimal.valueOf(1.10))
                .currency(Currency.getInstance('USD'))
                .createdAt(Instant.ofEpochMilli(1558808555000L))
                .build()

        when:
        def insertedTransfer = transferRepository.insert(transferToInsert)

        then:
        insertedTransfer != null
        and:
        insertedTransfer.id != null
        and:
        transferRepository.findById(insertedTransfer.getId()).get() == insertedTransfer

        cleanup:
        transferRepository.deleteById(insertedTransfer.getId())
    }

    def "should update transfer"() {
        given:
        def transferId = UUID.fromString('beeaedce-12b8-4739-8904-bb5c53acc32c')
        def existingTransfer = transferRepository.findById(transferId).get()
        def updateRequest = existingTransfer.toBuilder()
                .amount(BigDecimal.valueOf(150.55))
                .status(TransferStatus.FINISHED)
                .build()

        when:
        def updatedTransfer = transferRepository.update(updateRequest)

        then:
        updateRequest == updatedTransfer
        and:
        transferRepository.findById(transferId).get() == updatedTransfer

        cleanup:
        transferRepository.update(existingTransfer)
    }

    def "deleteById "() {
        given:
        def transferId = UUID.fromString('1d0f8741-aeb8-4dd6-9faf-f7289636a042')
        def existingTransfer = transferRepository.findById(transferId)

        expect:
        existingTransfer.isPresent()

        when:
        transferRepository.deleteById(transferId)

        then:
        !transferRepository.findById(transferId).isPresent()

        cleanup:
        transferRepository.insert(existingTransfer.get())
    }
}
