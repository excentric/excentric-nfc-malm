import * as fs from 'fs';
import * as path from 'path';
import * as os from 'os';

// Define the AlbumMetadata interface based on the Kotlin class
export interface AlbumMetadata {
    title: string;
    artist?: string;
    year?: number;
    appleMusicAlbumId?: string;
    slot?: string;
}

// Function to read all metadata files
export function readAllMetadataFiles(): AlbumMetadata[] {
    const userHome = os.homedir();
    const metadataDir = path.join(userHome, '.malm', 'metadata');
    const metadataFiles: AlbumMetadata[] = [];

    try {
        if (!fs.existsSync(metadataDir)) {
            console.error(`Metadata directory does not exist: ${metadataDir}`);
            return [];
        }

        // Read all JSON files in the directory
        const files = fs.readdirSync(metadataDir)
            .filter(file => file.endsWith('.json'));

        console.log(`Found ${files.length} Albums to write:`);

        for (const file of files) {
            const filePath = path.join(metadataDir, file);
            const fileContent = fs.readFileSync(filePath, 'utf8');
            const metadata = JSON.parse(fileContent) as AlbumMetadata;
            metadata.slot = path.basename(file, '.json')
            metadataFiles.push(metadata);
        }

        console.log("");

    } catch (error) {
        console.error('Error reading metadata files:', error);
    }

    return metadataFiles;
}
