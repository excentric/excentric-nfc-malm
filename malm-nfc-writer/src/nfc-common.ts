// Common types and interfaces for NFC operations

// Define types for the objects we're using
export type NFCReader = {
    reader: {
        name: string;
    };
    read(blockNumber: number, length: number, blockSize?: number, packetSize?: number, readClass?: number): Promise<Buffer>;
    write(blockNumber: number, data: Buffer, blockSize?: number, packetSize?: number): Promise<boolean>;
    on(event: string, listener: (...args: any[]) => void): void;
};

export type NFCCard = {
    atr: Buffer;
    standard: string;
    type: string;
    uid: string;
};

export type NDEFMessage = {
    type?: string;
    text: string;
    language?: string;
    [key: string]: any;
}

// Common NFC module imports
export const { NFC } = require('nfc-pcsc');
export const nfcCard = require('nfccard-tool');
