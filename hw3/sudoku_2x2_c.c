#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
bool check(char *test_c_data, int index){
    char ans = test_c_data[index];
    int row = index / 4;
    int col = index % 4;
    // rule1
    for(int i=0; i<4; i++){
        if((row*4 + i != index) && test_c_data[row*4 + i] == ans){
            return false;
        }
    }
    // rule2
    for(int i=0; i<4; i++){
        if((i*4 + col != index) && test_c_data[i*4 + col] == ans){
            return false;
        }
    }
    // rule3
    int blk = (index/8)*8 + (col/2)*2;
    for(int i=0; i<4; i++){
        if((blk + (i/2)*4 + (i%2) != index) && test_c_data[blk + (i/2)*4 + (i%2)] == ans){
            return false;
        }
    }
    return true;
}

bool solve(char *test_c_data, int index){
    if(index >= 16) return true;
    if(test_c_data[index] != '0'){
        return solve(test_c_data, index+1);
    }
    for(int guess=1; guess<=4; guess++){
        char tmp = '0' + guess;
        test_c_data[index] = tmp;
        if(check(test_c_data, index) && solve(test_c_data, index+1)){
            return true;
        }
    }
    test_c_data[index] = '0';
    return false;
}
void sudoku_2x2_c(char *test_c_data){
    for( int i=0 ; i<16 ; i++) {   
        char tmp = '0' + test_c_data[i];
        test_c_data[i] = tmp;
    }
    solve(test_c_data, 0);
    for( int i=0 ; i<16 ; i++) {
        int num = test_c_data[i] - '0';
        test_c_data[i] = num;
    }
    return;
// TODO
// Finish your sudoku algorithm in c language

}