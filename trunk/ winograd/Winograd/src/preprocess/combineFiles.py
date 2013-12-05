import os

folder = '/users/yzcchen/chen3/Winograd/Winograd/tokenized/'

allFile = open('giga.token', 'w')

for subF in os.listdir(folder):
    subFPath = folder + subF 
    for fn in os.listdir(subFPath):
        if fn.endswith('.text'):
            wFn = subFPath + '/' + fn
            fi = open(wFn, 'r')
            
            while 1:
                line = fi.readline()
                if not line:
                    break
                allFile.write(line)
            fi.close()
            print wFn
    
allFile.close()