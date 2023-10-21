#include "cubiomes/generator.h"
#include "cubiomes/finders.h"
#include <stdio.h>
#include <string.h>

#ifdef _WIN32
    #include <io.h>
    #include <fcntl.h>

    #define SET_BINARY_MODE(handle) _setmode(_fileno(handle), _O_BINARY)
#else
    #define SET_BINARY_MODE(handle) ((void)0)
#endif

#define CHUNK_SIZE     16
#define DIMENSION      DIM_END
#define STRUCTURE_TYPE End_City

#define FATAL(message) do { \
    fprintf(stderr, "fatal: " message); \
    fflush(stderr); \
    return -1; \
} while (0)

#define SEND(buffer, count) do { \
    fwrite(&buffer[0], 1, count, stdout); \
    fflush(stdout); \
} while (0)

#define FAILED_TO_SET_BINARY_MODE "could not open stdin in byte mode\n"
#define CONNECTION_CLOSED "client forcefully aborted the connection\n"


int sendEndCityCandidate(int x, int z) {
    unsigned char buffer[1 + sizeof(int) + sizeof(int)] = { 0 };

    buffer[0] = 1;

    memcpy(&buffer[1], &x, sizeof(int));
    memcpy(&buffer[1 + sizeof(int)], &z, sizeof(int));

    for (int i = 0; i < 1 + sizeof(int) + sizeof(int); i++) {
        fprintf(stderr, "%d, ", buffer[i]);
    }
    fprintf(stderr, "\n");
    fflush(stderr);

    SEND(buffer, 1 + sizeof(int) + sizeof(int));

    unsigned char success;
    if (fread(&success, 1, 1, stdin) == 0) FATAL(CONNECTION_CLOSED);

    return success;
}

void sendNoCandidates() {
    unsigned char buffer[1 + sizeof(int) + sizeof(int)] = { 0 };

    SEND(buffer, 1 + sizeof(int) + sizeof(int));
}


int didFindCity(Pos3 *pos3, int x, int z, Generator *g, int version, uint64_t seed) {
    Pos pos;

    if (!getStructurePos(STRUCTURE_TYPE, version, seed, x, z, &pos))
        return 0;

    if (!isViableStructurePos(STRUCTURE_TYPE, g, pos.x, pos.z, 0))
        return 0;

    SurfaceNoise surfaceNoise;
    initSurfaceNoise(&surfaceNoise, DIMENSION, seed);

    if (!isViableEndCityTerrain(g, &surfaceNoise, pos.x, pos.z))
        return 0;

    Piece pieces[END_CITY_PIECES_MAX] = {0};
    int piecesCount = getEndCityPieces(pieces, seed, pos.x >> 4, pos.z >> 4);

    for (int i = 0; i < piecesCount; i++) {
        Piece *piece = &pieces[i];

        if (strcmp(piece->name, "ship") == 0) {

            int rotationMatrix[4][3] = {
                {  5, 5,  6 },
                { -8, 5,  5 },
                { -7, 5, -8 },
                {  6, 5, -7 }
            };

            int *elytraLoc = rotationMatrix[piece->rot];

            pos3->x = piece->pos.x + elytraLoc[0];
            pos3->y = piece->pos.y + elytraLoc[1];
            pos3->z = piece->pos.z + elytraLoc[2];

            return 1;
        }
    }

    return 0;
}


// range is in regions
void findNearestEndCity(int x, int z, int range, int version, uint64_t seed) {
    fprintf(stderr, "finding an end city near %d, %d\n", x, z);
    fflush(stderr);

    Pos3 pos;

    Generator generator;
    setupGenerator(&generator, version, 0);
    applySeed(&generator, DIMENSION, seed);

    StructureConfig structureConfig;
    if (!getStructureConfig(STRUCTURE_TYPE, version, &structureConfig))
        return;
    
    double blocksPerRegion = structureConfig.regionSize * CHUNK_SIZE;

    // u - region x
    int origU = (int) floor(x / blocksPerRegion);
    // w - region z
    int origW = (int) floor(z / blocksPerRegion);

    int u = origU;
    int w = origW;

    /*fprintf(stderr, "checking %d, %d\n", u, w);
    fflush(stderr);*/

    if (didFindCity(&pos, u, w, &generator, version, seed)) {
        fprintf(stderr, "found end city with elytra at %d, %d\n", pos.x, pos.z);
        fflush(stderr);

        if (sendEndCityCandidate(pos.x, pos.z) == 1) return;
    }

    for (int radius = 1; radius <= range; radius++) {
        u = origU;
        w = origW - radius;

        int du;
        int dw;

        const int deltas[4][2] = {
            { +1 , +1 },
            { -1 , +1 },
            { -1 , -1 },
            { +1 , -1 }
        };

        for (int i = 0; i < 4; i++) {
            du = deltas[i][0];
            dw = deltas[i][1];

            for (int j = 0; j < radius; j++) {
                /*fprintf(stderr, "checking %d, %d (%d, %d)\n", u, w, u - origU, w - origW);
                fflush(stderr);*/

                if (didFindCity(&pos, u, w, &generator, version, seed)) {
                    fprintf(stderr, "found end city with elytra at %d, %d\n", pos.x, pos.z);
                    fflush(stderr);

                    if (sendEndCityCandidate(pos.x, pos.z) == 1) return;
                }

                u += du;
                w += dw;
            }
        }
    }

    sendNoCandidates();
}


int readHeader(int *version, uint64_t *seed) {
    unsigned char buffer[sizeof(int) + sizeof(uint64_t)];

    if (fread(&buffer[0], 1, sizeof(int) + sizeof(uint64_t), stdin) == 0) FATAL(CONNECTION_CLOSED);

    *version = *(int*) &buffer[0];
    *seed = *(uint64_t*) &buffer[sizeof(int)];

    fprintf(stderr, "received header!\n version: %d\n seed: %lld\n", *version, *seed);
    fflush(stderr);

    return 0;
}

int readRequest(int *x, int *z) {
    unsigned char buffer[sizeof(int) + sizeof(int)];

    if (fread(&buffer[0], 1, sizeof(int) + sizeof(int), stdin) == 0) FATAL(CONNECTION_CLOSED);

    *x = *(int*) &buffer[0];
    *z = *(int*) &buffer[sizeof(int)];

    return 0;
}


int main() {
    fprintf(stderr, "server started!\n");
    fflush(stderr);

    if (SET_BINARY_MODE(stdin) == -1)  FATAL(FAILED_TO_SET_BINARY_MODE);
    if (SET_BINARY_MODE(stdout) == -1) FATAL(FAILED_TO_SET_BINARY_MODE);

    int version;
    uint64_t seed;

    if (readHeader(&version, &seed) != 0) return -1;

    for (;;) {
        int x, z;

        if (readRequest(&x, &z) != 0) return -1;

        findNearestEndCity(x, z, 20, version, seed);
    }
}