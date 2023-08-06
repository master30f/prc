#include "cubiomes/generator.h"
#include "cubiomes/finders.h"
#include <stdio.h>

#define CHUNK_SIZE 16

void searchForCity(int rBegin, int rRadius, )

// range is in chunks
int findNearestEndCity(int x, int z, int range, int version, uint64_t seed) {
    int dimension = DIM_END;
    int structureType = End_City;

    Generator generator;
    setupGenerator(&generator, version, 0);
    applySeed(&generator, dimension, seed);

    SurfaceNoise surfaceNoise;
    initSurfaceNoise(&surfaceNoise, dimension, seed);

    StructureConfig structureConfig;
    if (!getStructureConfig(structureType, version, &structureConfig))
        return -1;
    
    double blocksPerRegion = structureConfig.regionSize * CHUNK_SIZE;
    int regionRange = (int) floor(range / structureConfig.regionSize);

    // u - region x
    int u = (int) floor(x / blocksPerRegion);
    // w - region z
    int w = (int) floor(z / blocksPerRegion);


}

int main() {
    Generator g;
    setupGenerator(&g, MC_1_20, 0);

    uint64_t seed = 0;



    return 0;
}