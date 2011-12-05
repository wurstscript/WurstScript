#include "stdafx.h"
#include <stdio.h>
#include <iostream>
#include "SFmpq_static.h"
#include <stdlib.h>
#include <string>


using namespace std;

// see http://sfsrealm.hopto.org/inside_mopaq/chapter4.htm for sfmpq documentation

// print some help
void printHelp() {
	std::cout << "Usages:\n";
	std::cout << "mpqcl.exe extract <path_to_map> <file_to_extraxt> <where to store file>\n";
	std::cout << "mpqcl.exe inject <path_to_map> <path_in_mpq> <where to get file>\n";
}


int mpqExtractFile(char* mpqArchive, char* fileToExtract, char* outputPath) {
	

	// open the map archive:	
	MPQHANDLE mpq = 0;
	bool ok = SFileOpenArchive(mpqArchive, 0, SFILE_OPEN_HARD_DISK_FILE, &mpq);
	if (!ok) {
		std::cout << "Could not open file.\n";
	} else {
		// open the file
		MPQHANDLE file = 0;
		ok = SFileOpenFile(fileToExtract, & file);
		if (!ok) {
			std::cout << "Could not open file to extract.";
		} else {
			// read file into buffer
			int filesize = SFileGetFileSize(file, 0);
			LPDWORD bytesRead = 0;
			void* buffer = malloc(filesize*sizeof(byte));
			ok = SFileReadFile(file, buffer, filesize, bytesRead, NULL);
			if (!ok) {
				std::cout << "Error while reading file...";
			} else {
				// write buffer to disc:
				FILE* file = fopen(outputPath, "w");
				fwrite(buffer, sizeof(byte), filesize, file);
				fclose(file);
			}
			SFileCloseFile(file);
		}
		SFileCloseArchive(mpq);
	}
	return 0;
}

int mpqInjectFile(char* mpqArchive, char* name, char* injectedFilePath) {
	int maxFiles = 1337;
	// open the map archive:
	MPQHANDLE mpq = MpqOpenArchiveForUpdate(mpqArchive, MOAU_OPEN_EXISTING, 0);
	
	if (mpq == NULL) {
		std::cout << "Could not open file.\n";
	} else {
		// add file
		bool ok = MpqAddFileToArchive(mpq, injectedFilePath, name, MAFA_REPLACE_EXISTING);
		if (!ok) {
			std::cout << "could not add file";
		}
		// compact & close
		ok = MpqCompactArchive(mpq);
		if (!ok) {
			std::cout << "could not compact archive";
		}
		ok = MpqCloseUpdatedArchive(mpq, NULL);
		if (!ok) {
			std::cout << "could not close archive";
		}
	}
	return 0;
}

int mpqDeleteFile(char* mpqArchive, char* name) {
	int maxFiles = 1337;
	// open the map archive:
	MPQHANDLE mpq = MpqOpenArchiveForUpdate(mpqArchive, MOAU_OPEN_EXISTING, 0);
	
	if (mpq == NULL) {
		std::cout << "Could not open file.\n";
	} else {
		// remove the file
		bool ok = MpqDeleteFile(mpq, name);
		if (!ok) {
			std::cout << "could not remove file";
		}
		// compact & close
		ok = MpqCompactArchive(mpq);
		if (!ok) {
			std::cout << "could not compact archive";
		}
		ok = MpqCloseUpdatedArchive(mpq, NULL);
		if (!ok) {
			std::cout << "could not close archive";
		}
	}
	return 0;
}


int main(int argc, char* argv[]) // fuck unicode support, thats too high for me (I really tried it though)
{
	if (argc > 1) {
		char* command = argv[1];
		if (argc == 5 && strcmp(command, "extract") == 0) { // magic constant 0 means strings are equal
			
			char* mapFile =argv[2];
			char* fileToExtract = argv[3];
			char* outputPath = argv[4];
			return mpqExtractFile(mapFile, fileToExtract, outputPath);
		}
		if (argc == 5 && strcmp(command, "inject") == 0) {
			return mpqInjectFile(argv[2], argv[3], argv[4]);
		}
		if (argc == 4 && strcmp(command, "delete") == 0) {
			return mpqDeleteFile(argv[2], argv[3]);
		}
	}

	printHelp();
	return 0;
}

