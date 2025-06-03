import {makeSonosRequest} from "./index";

const {NFC} = require('nfc-pcsc');
const nfcCard = require('nfccard-tool');
const http = require('http');

// Define types for the objects we're using
type NFCReader = {
    reader: {
        name: string;
    };
    read(blockNumber: number, length: number, blockSize?: number, packetSize?: number, readClass?: number): Promise<Buffer>;
    on(event: string, listener: (...args: any[]) => void): void;
};

type NFCCard = {
    atr: Buffer;
    standard: string;
    type: string;
    uid: string;
};

type NDEFMessage = {
    text: string;
    [key: string]: any;
}

const nfc = new NFC();

nfc.on('reader', (reader: NFCReader) => {

    console.log(`${reader.reader.name}  device attached`);

    reader.on('card', async (card: NFCCard) => {

        console.log(`card detected`, card);

        try {
            const cardHeader: Buffer = await reader.read(0, 20);

            const tag = nfcCard.parseInfo(cardHeader);
            console.log('tag info:', JSON.stringify(tag));


            if (nfcCard.isFormatedAsNDEF() && nfcCard.hasReadPermissions() && nfcCard.hasNDEFMessage()) {
                const NDEFRawMessage: Buffer = await reader.read(4, nfcCard.getNDEFMessageLengthToRead()); // starts reading in block 0 until 6
                const NDEFMessage: NDEFMessage[] = nfcCard.parseNDEF(NDEFRawMessage);

                makeSonosRequest(NDEFMessage[0].text)
                console.log('track:', NDEFMessage[0].text);
            } else {
                console.log('Could not parse anything from this tag: \n The tag is either empty, locked, has a wrong NDEF format or is unreadable.');
            }

        } catch (err: unknown) {
            console.error(`error when reading data`, err);
        }
    });

    reader.on('card.off', (card: NFCCard) => {
        console.log(`${reader.reader.name}  card removed`, card);
    });

    reader.on('error', (err: Error) => {
        console.log(`${reader.reader.name}  an error occurred`, err);
    });

    reader.on('end', () => {
        console.log(`${reader.reader.name}  device removed`);
    });

});

nfc.on('error', (err: Error) => {
    console.log('an error occurred', err);
});
