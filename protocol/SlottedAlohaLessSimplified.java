package protocol;

import java.util.Random;

/**
 * Created by Sophie on 2/20/14.
 */
public class SlottedAlohaLessSimplified implements IMACProtocol {

    @Override
    public TransmissionInfo TimeslotAvailable(MediumState previousMediumState,
                                              int controlInformation, int localQueueLength) {
        // No data to send, just be quiet
        if (localQueueLength == 0) {
            return new TransmissionInfo(TransmissionType.Silent, 0);
        }

        // Randomly transmit
        if (new Random().nextInt(4) == 0) {
            return new TransmissionInfo(TransmissionType.Data, 0);
        } else {
            return new TransmissionInfo(TransmissionType.Silent, 0);
        }

    }

}