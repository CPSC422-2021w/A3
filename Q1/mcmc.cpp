#include <iostream>
#include <stdio.h>
#include <vector>
#include <random>
#include <map>
#include <fstream>

using namespace std;

const int NUM_SAMPLES = 500;
int counter = 0;
int a0Count = 0;
int a1Count = 0; 

enum class NODE {
    A = 0,
    B = 1,
    C = 2,
    D = 3
};

struct node {
    public:
    float value;

    node() {};
    node(float v) {
        value = v;
    }
};

struct Nodes {
    node A;
    node B;
    node C;
    node D;
};

// [D, A]
map<pair<int, int>, int> factor4 = {
    { {0, 0}, 100 },
    { {0, 1}, 1 },
    { {1, 0}, 1 },
    { {1, 1}, 100 }
};
// [A, B]
map<pair<int, int>, int> factor1 = {
    { {0, 0}, 30 },
    { {0, 1}, 5 },
    { {1, 0}, 1 },
    { {1, 1}, 10 }
};

// [B, C]
map<pair<int, int>, int> factor2 = {
    { {0, 0}, 100 },
    { {0, 1}, 1 },
    { {1, 0}, 1 },
    { {1, 1}, 100 }
};

// [C, D]
map<pair<int, int>, int> factor3 = {
    { {0, 0}, 1 },
    { {0, 1}, 100 },
    { {1, 0}, 100 },
    { {1, 1}, 1 }
};

float generateRandomNumber() {
    std::random_device rd; // obtain a random number from hardware
    std::mt19937 gen(rd()); // seed the generator
    std::uniform_real_distribution<> distr(0, 1); // define the range
    return distr(gen);
}

NODE getNodeToSample() {
    int idx = counter % 4;
    counter++;
    if (idx == 0) {
        return NODE::A;
    }
    if (idx == 1) {
        return NODE::B;
    }
    if (idx == 2) {
        return NODE::C;
    }
    if (idx == 3) {
        return NODE::D;
    }
}

void approximateInference(vector<Nodes>& allSamples, NODE evidence, ofstream& myFile) {
    myFile.open("gibbs-output.csv");
    int samples = 0;
    for(int i = 0; i < NUM_SAMPLES; i++) {
        printf("\n\n\ncurrent assignment:\n");
        printf("A = %f, B = %f,C = %f, D = %f\n", allSamples.back().A.value, allSamples.back().B.value, 
                                                  allSamples.back().C.value, allSamples.back().D.value);

        Nodes& currentSample = allSamples.back();
        Nodes newSample = currentSample;
        NODE nodeToSample = getNodeToSample();

        if (nodeToSample == NODE::A && nodeToSample != evidence) {
            samples++;
            printf("sampling node A\n");
            // factors involving A are 1(AB) and 4(DA)
            pair<int, int> f4;
            pair<int, int> f1;
            pair<float, float> pDistrA;
            f4.first = factor4.at( {currentSample.D.value, 0} );
            f4.second = factor4.at( {currentSample.D.value, 1} );
            f1.first = factor1.at( {0, currentSample.B.value} );
            f1.second = factor1.at( {1, currentSample.B.value} );
            
            pDistrA = {f4.first * f1.first, f4.second * f1.second };
            float normalizedA0 =  pDistrA.first / (pDistrA.first + pDistrA.second);
            float normalizedA1 = pDistrA.second / (pDistrA.first + pDistrA.second);
            pDistrA = {normalizedA0, normalizedA1};
            printf("sample distribution of A: (%f, %f)\n", pDistrA.first, pDistrA.second);
            float newSampleA;
            float randNum = generateRandomNumber();
            printf("random number for sampling A: %f\n", randNum);
            if (randNum >= 0 && randNum <= pDistrA.first) {
                newSampleA = 0;
                a0Count++;
            } else {
                a1Count++;
                newSampleA = 1;
            }
            newSample.A.value = newSampleA;
            printf("new sample after sampling A: \n");
            printf("A = %f, B = %f,C = %f, D = %f\n", newSample.A.value, newSample.B.value, 
                                                  newSample.C.value, newSample.D.value);
            allSamples.push_back(newSample);
            myFile << samples << ", ";
            float probA0 = (float)a0Count / ((float)a0Count + (float)a1Count);
            printf("probA0: %f\n", probA0);
            myFile << probA0 << "\n";

        } else if (nodeToSample == NODE::B && nodeToSample != evidence) {
            printf("sampling node B\n");
            samples++;
            // factors involving B are 1(AB) and 2(BC)
            pair<int, int> f1;
            pair<int, int> f2;
            pair<float, float> pDistrB;
            f1.first = factor1.at( {currentSample.A.value, 0} );
            f1.second = factor1.at( {currentSample.A.value, 1} );
            f2.first = factor2.at( {0, currentSample.C.value} );
            f2.second = factor2.at( {1, currentSample.C.value} );
            
            pDistrB = {f1.first * f2.first, f1.second * f2.second };
            float normalizedB0 =  pDistrB.first / (pDistrB.first + pDistrB.second);
            float normalizedB1 = pDistrB.second / (pDistrB.first + pDistrB.second);
            pDistrB = {normalizedB0, normalizedB1};
            printf("sample distribution of B: (%f, %f)\n", pDistrB.first, pDistrB.second);
            float newSampleB;
            float randNum = generateRandomNumber();
            printf("random number for sampling B: %f\n", randNum);
            if (randNum >= 0 && randNum <= pDistrB.first) {
                newSampleB = 0;
            } else {
                newSampleB = 1;
            }
            newSample.B.value = newSampleB;
            printf("new sample after sampling B: \n");
            printf("A = %f, B = %f,C = %f, D = %f\n", newSample.A.value, newSample.B.value, 
                                                  newSample.C.value, newSample.D.value);
            if (newSample.A.value <= 0.f) {
                a0Count++;
            } else {
                a1Count++;
            }
            allSamples.push_back(newSample);
            myFile << samples << ", ";
            float probA0 = (float)a0Count / ((float)a0Count + (float)a1Count);
            printf("probA0: %f\n", probA0);
            myFile << probA0 << "\n";

        } else if (nodeToSample == NODE::C && nodeToSample != evidence) {
            printf("sampling node C\n");
            samples++;
            // factors involving C are 2(BC) and 3(CD)
            pair<int, int> f2;
            pair<int, int> f3;
            pair<float, float> pDistrC;
            f2.first = factor2.at( {currentSample.B.value, 0} );
            f2.second = factor2.at( {currentSample.B.value, 1} );
            f3.first = factor3.at( {0, currentSample.D.value} );
            f3.second = factor3.at( {1, currentSample.D.value} );
            
            pDistrC = {f2.first * f3.first, f2.second * f3.second };
            float normalizedC0 =  pDistrC.first / (pDistrC.first + pDistrC.second);
            float normalizedC1 = pDistrC.second / (pDistrC.first + pDistrC.second);
            pDistrC = {normalizedC0, normalizedC1};
            printf("sample distribution of C: (%f, %f)\n", pDistrC.first, pDistrC.second);
            float newSampleC;
            float randNum = generateRandomNumber();
            printf("random number for sampling C: %f\n", randNum);
            if (randNum >= 0 && randNum <= pDistrC.first) {
                newSampleC = 0;
            } else {
                newSampleC = 1;
            }
            newSample.C.value = newSampleC;
            printf("new sample after sampling C: \n");
            printf("A = %f, B = %f,C = %f, D = %f\n", newSample.A.value, newSample.B.value, 
                                                 newSample.C.value, newSample.D.value);
            if (newSample.A.value <= 0.f) {
                a0Count++;
            } else {
                a1Count++;
            }
            allSamples.push_back(newSample);
            myFile << samples << ", ";
            float probA0 = (float)a0Count / ((float)a0Count + (float)a1Count);
            printf("probA0: %f\n", probA0);
            myFile << probA0 << "\n";

        } else if (nodeToSample == NODE::D && nodeToSample != evidence) {
            printf("sampling node D\n");
            samples++;
            // factors involving D are 3(CD) and 4(DA)
            pair<int, int> f3;
            pair<int, int> f4;
            pair<float, float> pDistrD;
            f3.first = factor3.at( {currentSample.C.value, 0} );
            f3.second = factor3.at( {currentSample.C.value, 1} );
            f4.first = factor4.at( {0, currentSample.A.value} );
            f4.second = factor4.at( {1, currentSample.A.value} );
            
            pDistrD = {f3.first * f4.first, f3.second * f4.second };
            float normalizedD0 =  pDistrD.first / (pDistrD.first + pDistrD.second);
            float normalizedD1 = pDistrD.second / (pDistrD.first + pDistrD.second);
            pDistrD = {normalizedD0, normalizedD1};
            printf("sample distribution of D: (%f, %f)\n", pDistrD.first, pDistrD.second);
            float newSampleD;
            float randNum = generateRandomNumber();
            printf("random number for sampling D: %f\n", randNum);
            if (randNum >= 0 && randNum <= pDistrD.first) {
                newSampleD = 0;
            } else {
                newSampleD = 1;
            }
            newSample.D.value = newSampleD;
            printf("new sample after sampling D: \n");
            printf("A = %f, B = %f,C = %f, D = %f\n", newSample.A.value, newSample.B.value, 
                                                 newSample.C.value, newSample.D.value);
            if (newSample.A.value <= 0.f) {
                a0Count++;
            } else {
                a1Count++;
            }
            allSamples.push_back(newSample);
            myFile << samples << ", ";
            float probA0 = (float)a0Count / ((float)a0Count + (float)a1Count);
            printf("probA0: %f\n", probA0);
            myFile << probA0 << "\n";
        }
    }
    myFile.close();
}

int main() {
    ofstream myfile;
    Nodes current;
    current.A.value = 0;
    current.B.value = 1.f;
    current.C.value = 0;
    current.D.value = 0.f;
    vector<Nodes> allSamples;
    allSamples.push_back(current);
    // printf("initial assignment:\n");
    // printf("A = %f, B = %f,C = %f, D = %f\n", current.A.value, current.B.value, current.C.value, current.D.value);
    approximateInference(allSamples, NODE::B, myfile);

    printf("aoCount: %d\na1Count:%d\n", a0Count, a1Count); 
}