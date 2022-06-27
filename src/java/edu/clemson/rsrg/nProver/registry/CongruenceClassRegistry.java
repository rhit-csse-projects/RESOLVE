package edu.clemson.rsrg.nProver.registry;

import java.util.*;

/**
 * <p>Congruence class registry, a key component in the nProver. It stores the sequent VC and performs all the manipulations
 * necessary to prove the VC's correctness.</p>
 *
 * @author Nicodemus Msafiri J. M.
 * @version v1.0
 *
 * @param <T1> type of tree node label selected
 * @param <T2>
 * @param <T3>
 * @param <T4>
 */

public class CongruenceClassRegistry <T1,T2,T3,T4>{ //T1 - Tree node label T2 -

    //========================================== UPPER BOUNDS =============================================
    /**
     *
     */
    private int ccDesignatorCapacity;
    private int cClusterDesignatorCapacity;
    private int argumentListCapacity;
    private int rootLabelCapacity;

    //=========================================TOP VALUES=========================================================
    //Top_CC_Dsntr
    private int topCongruenceClassDesignator;
    //Top_Cr_Dsntr
    private int topCongruenceClusterDesignator;
    //Rt_Lab_Capacity
    private int topLabelCapacity;

    //========================================= REGISTRY STRUCTURES =========================================
    private VarietyList[] varietyArray;
    private Plantation[] plantationArray;
    private CongruenceCluster[] clusterArray;
    private CongruenceClass[] congruenceClassArray;
    private ClusterArgument [] clusterArgumentArray;
    private Queue<Integer> clusterArgumentString;
    private Queue<Integer> classMergeList;
    private Set<Integer> succedentReflexiveOperatorsSet;
    //private CongruenceClassAttribute<T4> classAttributeMap;

    //we can take this as plantation designator determines the index in plantation array and index to plantation tag
    private int indexForPlantationArray;
    //local available index to use for the cluster array
    private int topArgStrArrIndex = 2;

    //temp flag, this does not exist in the concept yet but, it will be added as it is necessary
    private boolean isProved = false;
    private boolean succedentReflexiveOperatorTest = false;


    /**
     * <p>The constructor for the registry</p>
     *
     * @param ccDesignatorCapacity
     * @param cClusterDesignatorCapacity
     * @param argumentListCapacity
     * @param rootLabelCapacity
     */
    public CongruenceClassRegistry(int ccDesignatorCapacity, int cClusterDesignatorCapacity, int argumentListCapacity, int rootLabelCapacity){
        this.ccDesignatorCapacity = ccDesignatorCapacity;
        this.cClusterDesignatorCapacity = cClusterDesignatorCapacity;
        this.argumentListCapacity = argumentListCapacity;
        this.rootLabelCapacity = rootLabelCapacity;

        topCongruenceClassDesignator = 0;
        topCongruenceClusterDesignator = 0;
        indexForPlantationArray = 0;
        topLabelCapacity = 0;

        varietyArray = new VarietyList[rootLabelCapacity];
        plantationArray = new Plantation[rootLabelCapacity];
        clusterArray = new CongruenceCluster[cClusterDesignatorCapacity];
        congruenceClassArray = new CongruenceClass[ccDesignatorCapacity];
        clusterArgumentArray = new ClusterArgument[100];
        clusterArgumentString = new ArrayDeque<>();
        classMergeList = new ArrayDeque<>();
        succedentReflexiveOperatorsSet = new HashSet<>();

        //start the index 0 with {0,0,0,0,0,0}
        //create a cluster object, with 0 index to argument list then update later
        CongruenceCluster cCluster = new CongruenceCluster(0,0,0, 0, 0, 0, 0, 0);
        //put the created cluster into the cluster array
        clusterArray[0] = cCluster;
        //same thing for the argument array
        ClusterArgument cArgument = new ClusterArgument(0,0,0,0,0);
        clusterArgumentArray[0] = cArgument;

        //same thing for plantation
        Plantation plantation = new Plantation(0,0,0,0,0,0);
        //put the initial created plantation into the array
        plantationArray[0] = plantation;


    }

/************************************************* PUBLIC METHODS *******************************************************************/

    /**<p>The operation registers a new singleton class with one cluster, both are assigned a new designator and accessor to the
     * class is returned</p>
     *
     * @param treeNodeLabel an integer value to represent the tree node being registered.
     * @return integer value representing accessor for the class created.
     */
    public int registerCluster(Integer treeNodeLabel){ /*Register_Cluster_Lbld*/
        int nextWithSimilarArgString = 0;
        int nextPlantationCluster = 0;
        int prevPlantationCluster = 0;
        int nextCCPlantation = 0;
        int nextVrtyPlantation = 0;
        int prvVrtyPlantation = 0;

        //special Bingo check for reflexive operators in the succedent before we continue normally if the VC is not proved
        if(succedentReflexiveOperatorsSet.contains(treeNodeLabel)){
            Queue<Integer> tempArgList = new ArrayDeque<>();
            Integer tempClassDesignator;
            int iter = clusterArgumentString.size();

            //create a copy as it will be needed if the VC is not proved and normal registration of reflexive operator is resumed.
            while(iter > 0){
                tempClassDesignator = clusterArgumentString.remove();
                tempArgList.add(tempClassDesignator);
                clusterArgumentString.add(tempClassDesignator);
                iter--;
            }

            //use an internal procedure to do what are congruent is doing, and call that inside are congruent operation
            //are congruent is meant for the client outside
            if(areClassesCongruent(tempArgList.remove(), tempArgList.remove())){
                isProved = true;
            }else{
                //for efficiency, this will tell the registry there is a reflexive operator in the succedent and the test should be activated otherwise don't waste any resources
                succedentReflexiveOperatorTest = true;
            }
        }

        if(isProved == false) {

            topCongruenceClassDesignator++;
            topCongruenceClusterDesignator++;
            indexForPlantationArray++;
            topLabelCapacity++;

            //this is the last position in the argument string array in terms of depth from the empty arg string
            int lastArgStringPos = 0;
            //create a plantation class object
            Plantation plantation = new Plantation(treeNodeLabel, indexForPlantationArray, indexForPlantationArray, nextCCPlantation, nextVrtyPlantation, prvVrtyPlantation);
            //put the created plantation into the plantation array
            plantationArray[indexForPlantationArray] = plantation;

            //create a congruence class object
            //indexInArgArray is the Arg string occurrence position, an index for the created arg string for this cluster
            CongruenceClass cClass = new CongruenceClass(indexForPlantationArray, topCongruenceClassDesignator, lastArgStringPos, topCongruenceClassDesignator);

            //put the created class into the congruence class array
            congruenceClassArray[topCongruenceClassDesignator] = cClass;

            //attach the attributes to this class using its cClass designator
            //classAttributeMap.addAClassAttribute(topCongruenceClassDesignator, classAttribute);

            //create a cluster object, with 0 index to argument list then update later
            CongruenceCluster cCluster = new CongruenceCluster(treeNodeLabel, 0, topCongruenceClassDesignator, topCongruenceClusterDesignator, nextPlantationCluster, prevPlantationCluster, topCongruenceClusterDesignator, nextWithSimilarArgString);

            //put the created cluster into the cluster array
            clusterArray[topCongruenceClusterDesignator] = cCluster;

            //get the index created after putting the argument string for this cluster
            int indexInArgArray = createClusterArgumentArray(treeNodeLabel, clusterArgumentString);
            //set the index to argument array, initially I created the cluster argument first before creating the object
            clusterArray[topCongruenceClusterDesignator].setIndexToArgumentList(indexInArgArray);

            //update variety list array
            addInVarietyListArray(treeNodeLabel, indexForPlantationArray, indexForPlantationArray);

            return topCongruenceClassDesignator;
        }else {
            return 0;
        }
    }


    //check if the cluster is already registered
    public boolean checkIfRegistered(Integer treeNodeLabel){ /*Is_Already_Reg_Clstr*/
        //this will need a check into the cluster argument array if the argument exists
        int classDesignator = 0;
        int nextClusterArgIndex = 0;
        int count = 0;
        int argStringLengh = argListLength(clusterArgumentString);
        int currentClusterArgIndex = 1;
        Queue<Integer> tempQueue = new ArrayDeque<>();
        if(argStringLengh == 0){//variable or constant
            if(clusterArgumentArray[currentClusterArgIndex] == null){ //nothing with one argument registered don't waste time
                return false;
            }else{//here we will check if the label and the argument is the same as one to be registered

                if(clusterArray[clusterArgumentArray[currentClusterArgIndex].getClusterNumber()].getTreeNodeLabel() == treeNodeLabel && clusterArray[clusterArgumentArray[currentClusterArgIndex].getClusterNumber()].getIndexToArgList() == currentClusterArgIndex){
                    return true;
                }
                //let us check all clusters that have one argument by following the next with same argument filed until the end
                //this is within the cluster array
                int currentClusterIndex = clusterArray[clusterArgumentArray[currentClusterArgIndex].getClusterNumber()].getNextWithSameArg();
                while (clusterArray[currentClusterIndex].getNextWithSameArg() != 0){
                    //this is within the cluster array
                    //int currentClusterIndex = clusterArray[clusterArgumentArray[currentClusterArgIndex].getClusterNumber()].getNextWithSameArg();
                    //clusterArgumentArray[currentClusterArgIndex].getClusterNumber()
                    if (clusterArray[currentClusterIndex].getTreeNodeLabel() == treeNodeLabel && clusterArray[currentClusterIndex].getIndexToArgList() == currentClusterArgIndex) {
                            return true;
                    }
                    currentClusterIndex = clusterArray[currentClusterIndex].getNextWithSameArg();
                    if (currentClusterIndex != 0) { //we don't want to index 0 in any of the structures we have
                    }else { //if it is 0 there is nothing more we can do, it is not there.
                        return false;
                    }
                }

            }
            return false;
        }
        //cluster we are checking has arguments and assumed already appended in the argument list
        while (clusterArgumentString.size() > 0){
            classDesignator = removeFirstArgDesignator();
            tempQueue.add(classDesignator);
            //this is added to solve the problem that some cluster argument might have disappeared when mearge happens
            classDesignator = getTheUltimateDominantClass(congruenceClassArray[classDesignator].getDominantCClass());

            nextClusterArgIndex = clusterArgumentArray[currentClusterArgIndex].getNextClusterArg();
            if(nextClusterArgIndex == 0) {
                //restore the cluster argument list
                while(tempQueue.size() > 0 ){
                    appendToClusterArgList(tempQueue.remove());
                }
                return false; //there is no next level
            }else {
                //next line is changed to check dominant class for all sides of the equals, initially it had  dominant class on the RHS only
                if(getTheUltimateDominantClass(congruenceClassArray[clusterArgumentArray[nextClusterArgIndex].getCcNumber()].getDominantCClass()) == classDesignator){
                    count++;
                    currentClusterArgIndex = nextClusterArgIndex;
                }else {
                    if( clusterArgumentArray[nextClusterArgIndex].getAlternativeArg() == 0){ //it didn't have even the first class, just return false and exit
                        //restore the cluster argument list
                        while(tempQueue.size() > 0 ){
                            appendToClusterArgList(tempQueue.remove());
                        }
                        return false;
                    }

                    while (clusterArgumentArray[nextClusterArgIndex].getAlternativeArg() != 0){ // we have hope it may be on the alternative args

                        if(getTheUltimateDominantClass(clusterArgumentArray[clusterArgumentArray[nextClusterArgIndex].getAlternativeArg()].getCcNumber()) == classDesignator){ // we found it, increase count and get out
                            count++;
                            //currentClusterArgIndex = nextClusterArgIndex;
                            //replaced it was not updated right
                            currentClusterArgIndex = clusterArgumentArray[nextClusterArgIndex].getAlternativeArg();
                            break;
                        }else {// we did not get it, check the next one
                            nextClusterArgIndex = clusterArgumentArray[nextClusterArgIndex].getAlternativeArg();
                        }
                    }
                }

            }
            if (argStringLengh == count){ //check if they have same length, then arg string exists. But we still have to check if the cluster exists by checking the label and the arg string
                //duplicate may need private method
                if (clusterArray[clusterArgumentArray[currentClusterArgIndex].getClusterNumber()].getTreeNodeLabel() == treeNodeLabel && clusterArray[clusterArgumentArray[currentClusterArgIndex].getClusterNumber()].getIndexToArgList() == currentClusterArgIndex){
                    //restore the cluster argument list
                    while(tempQueue.size() > 0 ){
                        appendToClusterArgList(tempQueue.remove());
                    }
                    return true;
                }
                while (clusterArray[clusterArgumentArray[currentClusterArgIndex].getClusterNumber()].getNextWithSameArg() != 0){
                    currentClusterArgIndex = clusterArray[clusterArgumentArray[currentClusterArgIndex].getClusterNumber()].getNextWithSameArg();
                    if (clusterArray[clusterArgumentArray[currentClusterArgIndex].getClusterNumber()].getTreeNodeLabel() == treeNodeLabel && clusterArray[clusterArgumentArray[currentClusterArgIndex].getClusterNumber()].getIndexToArgList() == currentClusterArgIndex){
                        //restore the cluster argument list
                        while(tempQueue.size() > 0 ){
                            appendToClusterArgList(tempQueue.remove());
                        }
                        return true;
                    }
                }
            }
        }
        //restore the cluster argument list
        while(tempQueue.size() > 0 ){
            appendToClusterArgList(tempQueue.remove());
        }
        return false;
    }


    //getting back the congruence class accessor for
    //Assumption the cluster is existing in a class
    //the argument string is appended with the argument classes for the label passed in the operation
    public int getAccessorFor(Integer treeNodeLabel){//Get_Accr_for
        int currentIndexInArgumentString = 1;
        int currentIndexInClusterArray = clusterArgumentArray[currentIndexInArgumentString].getClusterNumber();
        if(clusterArgumentString.size() == 0){
            //Assuming everything in the list of clusters with same arguments will point to currentIndexInArgumentString
            //And we should eventually find one with the tree node label we are looking for
            while(clusterArray[currentIndexInClusterArray].getTreeNodeLabel() != treeNodeLabel){
                currentIndexInClusterArray = clusterArray[currentIndexInClusterArray].getNextWithSameArg();
            }
            //return the class, this case I am returning the dominant class
            return congruenceClassArray[clusterArray[currentIndexInClusterArray].getIndexToCongruenceClass()].getDominantCClass();

        }else{//the cluster argument string is not empty we have to walk the argument array structure
            //Assuming the way the were put in the structure is maintained all throughout
            int finalCountNeeded = clusterArgumentString.size();
            //int iter = finalCountNeeded;
            int currentClassDesignator = 0;
            //currentClassDesignator = congruenceClassArray[currentClassDesignator].getDominantCClass();
            int nextClusterArgument = 1;
            int countArgumentsFound = 0;


            //the assumption is that we should find everything in the argument string, so I have written the code with that assumption, no need to keep the count
            while (clusterArgumentString.size() > 0){
                currentClassDesignator = removeFirstArgDesignator();
                currentClassDesignator = congruenceClassArray[currentClassDesignator].getDominantCClass();
                nextClusterArgument = clusterArgumentArray[nextClusterArgument].getNextClusterArg();

                //again next line is changed to check dominant class for all sides of the equals, initially it had  dominant class on the RHS only
                if (congruenceClassArray[clusterArgumentArray[nextClusterArgument].getCcNumber()].getDominantCClass() == currentClassDesignator){
                    countArgumentsFound++;
                    //break;
                }else{
                    while(clusterArgumentArray[nextClusterArgument].getAlternativeArg() != 0){
                        if(getTheUltimateDominantClass(clusterArgumentArray[clusterArgumentArray[nextClusterArgument].getAlternativeArg()].getCcNumber()) == currentClassDesignator){
                            countArgumentsFound++;
                            nextClusterArgument = clusterArgumentArray[nextClusterArgument].getAlternativeArg();
                            break;
                        }else{
                            nextClusterArgument = clusterArgumentArray[nextClusterArgument].getAlternativeArg();
                        }
                    }
                }
            }

            if(finalCountNeeded == countArgumentsFound){
                return congruenceClassArray[clusterArray[clusterArgumentArray[nextClusterArgument].getClusterNumber()].getIndexToCongruenceClass()].getDominantCClass();
            }else{
                return 0; //we should not get here if everything is set up correctly
            }
        }


    }

    //return the next class accessor after the current one provided in the variety
    //this information is kept by one of the field in the congruence class label
    public int advanceCClassAccessor(Integer treeNodeLabel, int currentCCAccessor) { //Advance_CC_Accr_for
        int currentPlantationForTreeNodeLabel = 0;
        int nextPlantationInNextClassAccessor = 0;

        currentPlantationForTreeNodeLabel = varietyArray[treeNodeLabel].getFirstPlantation();

        int congruenceClassForCluster = clusterArray[plantationArray[currentPlantationForTreeNodeLabel].getFirstPlantationCluster()].getIndexToCongruenceClass();
        int dominantCongruenceClassForCluster = congruenceClassArray[congruenceClassForCluster].getDominantCClass();

        while(currentPlantationForTreeNodeLabel != 0){
            if(congruenceClassForCluster == currentCCAccessor || dominantCongruenceClassForCluster == currentCCAccessor){
                nextPlantationInNextClassAccessor = plantationArray[currentPlantationForTreeNodeLabel].getNextVrtyPlantation();
                congruenceClassForCluster = clusterArray[plantationArray[nextPlantationInNextClassAccessor].getFirstPlantationCluster()].getIndexToCongruenceClass();
                dominantCongruenceClassForCluster = congruenceClassArray[congruenceClassForCluster].getDominantCClass();
                //I am returning dominant class here
                return dominantCongruenceClassForCluster;
            }else{
                currentPlantationForTreeNodeLabel = plantationArray[currentPlantationForTreeNodeLabel].getNextVrtyPlantation();
                if(currentPlantationForTreeNodeLabel != 0) {
                    congruenceClassForCluster = clusterArray[plantationArray[currentPlantationForTreeNodeLabel].getFirstPlantationCluster()].getIndexToCongruenceClass();
                    dominantCongruenceClassForCluster = congruenceClassArray[congruenceClassForCluster].getDominantCClass();
                }
            }
        }
        //this is just defensive, we will never get here as the operation is always called when we have next class
        return 0;
    }
    //There are no more classes in the variety to look for
    public boolean isVarietyMaximal(Integer treeNodeLabel, int currentCCAccessor){ /*Is_Vrty_Maximal_for*/
        int currentPlantationForTreeNodeLabel = 0;
        int nextPlantationInNextClassAccessor = 0;

        currentPlantationForTreeNodeLabel = varietyArray[treeNodeLabel].getFirstPlantation();

        int congruenceClassForCluster = clusterArray[plantationArray[currentPlantationForTreeNodeLabel].getFirstPlantationCluster()].getIndexToCongruenceClass();
        int dominantCongruenceClassForCluster = congruenceClassArray[congruenceClassForCluster].getDominantCClass();

        while(currentPlantationForTreeNodeLabel != 0){
            if(congruenceClassForCluster == currentCCAccessor || dominantCongruenceClassForCluster == currentCCAccessor){
                nextPlantationInNextClassAccessor = plantationArray[currentPlantationForTreeNodeLabel].getNextVrtyPlantation();
                if(nextPlantationInNextClassAccessor == 0){
                    return true;
                }else{
                    return false;
                }
            }else{
                currentPlantationForTreeNodeLabel = plantationArray[currentPlantationForTreeNodeLabel].getNextVrtyPlantation();
                if(currentPlantationForTreeNodeLabel != 0) {
                    congruenceClassForCluster = clusterArray[plantationArray[currentPlantationForTreeNodeLabel].getFirstPlantationCluster()].getIndexToCongruenceClass();
                    dominantCongruenceClassForCluster = congruenceClassArray[congruenceClassForCluster].getDominantCClass();
                }
            }
        }
        //this is just defensive, we hope it will be existing and decision will be made earlier
        return true;
    }

    //check if the supplied tree node label is among the root node labels
    public boolean isRegistryLabel(Integer treeNodeLabel){ /*Is_Rgry_Lab*/
        if(varietyArray[treeNodeLabel] != null){ //each node at some point is considered a root node label
            return true;
        }
        return false;
    }

    //return the difference, 0 if we have exhausted congruence class designators
    public int remainingCCDesignatorCap(){
        return ccDesignatorCapacity - topCongruenceClassDesignator;
    }

    public int remainingCClusterDesignatorCap(){
        return cClusterDesignatorCapacity - topCongruenceClusterDesignator;
    }

    public int remainingLabelCap(){
        return rootLabelCapacity - topLabelCapacity;
    }

    //should be next one within the plantation
    public int advanceClusterAccessor(Integer treeNodeLabel, int currentClusterAccessor){
        //we should have only one cluster designated by currentClusterAccessor,
        //it might not be the dominant, but definitely only one
        int dominantCluster = currentClusterAccessor;
        while(clusterArray[dominantCluster].getDominantCluster() != currentClusterAccessor){
            dominantCluster = clusterArray[dominantCluster].getDominantCluster();
        }
        return clusterArray[dominantCluster].getNextPlantationCluster();
    }

    //check if there is next cluster for the current plantation
    public boolean isPlantationMaximal(Integer treeNodeLabel, int currentClusterAccessor){
        int dominantCluster = currentClusterAccessor;
        while(clusterArray[dominantCluster].getDominantCluster() != currentClusterAccessor){
            dominantCluster = clusterArray[dominantCluster].getDominantCluster();
        }
        if (clusterArray[dominantCluster].getNextPlantationCluster() == 0){
            return true;
        }
        return false;
    }

    //it will check the dominant class, if the same, it is minimal otherwise it is not, and
    // make sure the tree node label provided is in the registry
    public boolean isMinimalVCCDesignator(Integer treeNodeLabel, int cClassAccessor){
        if(congruenceClassArray[cClassAccessor].getDominantCClass() == cClassAccessor && varietyArray[treeNodeLabel] != null){
            return true;
        }
        return false;
    }

    public boolean isMinimalPlantationClusterDesignator(Integer treeNodeLabel, int cClassAccessor, int clusterAccessor){
        if(clusterArray[clusterAccessor].getDominantCluster() == clusterAccessor && isMinimalVCCDesignator(treeNodeLabel, cClassAccessor)){
            return true;
        }
        return false;
    }

    //considering the last one on the right is the one added first
    public int removeFirstArgDesignator(){
        return clusterArgumentString.remove();
    }

    //get the current argument list length
    public int argListLength(Queue<Integer> clusterArgumentString){
        return clusterArgumentString.size();
    }

    //append at the front, the next one will push earlier one forward
    public void appendToClusterArgList(int cClassDesignator){
        clusterArgumentString.add(cClassDesignator);
    }

    //The first accessor and second accessor should be provided by user as dominant designators, but we check it anyways to be sure
    //assumes firstCCAccessor is smaller than secondCCAccessor always
    //first accessor = 3, second accessor = 7, just as example
    public void makeCongruent(int firstCCAccessor, int secondCCAccessor){

        int indexToArgString_3_L1 = 0;
        int indexToArgString_7_L2 = 0;
        int indexToArgString_3_L2 = 0;
        int indexTOArgString_7_L1 = 0;
        int level_2 = 2;
        int level_1 = 1;
        //make sure they are dominant classes
        firstCCAccessor = getTheUltimateDominantClass(congruenceClassArray[firstCCAccessor].getDominantCClass()); //on assumption that the path is compressed
        secondCCAccessor = getTheUltimateDominantClass(congruenceClassArray[secondCCAccessor].getDominantCClass()); //on assumption that the path is compressed

        //addFirst and removeFirst will operate on a FIFO
        classMergeList.add(firstCCAccessor);
        classMergeList.add(secondCCAccessor);


        BitSet bitSet = new BitSet();
        //if no classes are added during merging process it will only execute once. Otherwise, it will continue until the list is exhausted
        while(classMergeList.size() != 0){
            firstCCAccessor = classMergeList.remove();
            secondCCAccessor = classMergeList.remove();
            bitSet.clear();
            bitSet = mergeAttribute(firstCCAccessor,secondCCAccessor);
            //in either case we will have 4 values to help in narrowing the search
            if(firstCCAccessor < secondCCAccessor) {
                indexToArgString_3_L1 = congruenceClassArray[firstCCAccessor].getIndexInClusterArgArrayFromASOP(1);
                indexToArgString_7_L2 = congruenceClassArray[secondCCAccessor].getIndexInClusterArgArrayFromASOP(2);
                indexToArgString_3_L2 = congruenceClassArray[firstCCAccessor].getIndexInClusterArgArrayFromASOP(2);
                indexTOArgString_7_L1 = congruenceClassArray[secondCCAccessor].getIndexInClusterArgArrayFromASOP(1);
            }else{
                indexToArgString_3_L1 = congruenceClassArray[secondCCAccessor].getIndexInClusterArgArrayFromASOP(1);
                indexToArgString_7_L2 = congruenceClassArray[firstCCAccessor].getIndexInClusterArgArrayFromASOP(2);
                indexToArgString_3_L2 = congruenceClassArray[secondCCAccessor].getIndexInClusterArgArrayFromASOP(2);
                indexTOArgString_7_L1 = congruenceClassArray[firstCCAccessor].getIndexInClusterArgArrayFromASOP(1);
            }

            if(isSequentVCProvedByAttributes(bitSet)) { //check if it is proved
                //Do something if it is proved
                isProved = true;
            }else if(succedentReflexiveOperatorTest){
                    if(indexToArgString_7_L2 != 0 && indexToArgString_3_L1 != 0){
                        if(firstCCAccessor < secondCCAccessor){
                            if(reflexivityBingoTest(firstCCAccessor, secondCCAccessor, level_2)){
                                isProved = true;
                            }
                        }else{
                            if(reflexivityBingoTest(secondCCAccessor, firstCCAccessor, level_2)){
                                isProved = true;
                            }
                        }
                    }else if(indexTOArgString_7_L1 != 0 && indexToArgString_3_L2 != 0 ){
                        if(firstCCAccessor < secondCCAccessor){
                            if(reflexivityBingoTest(firstCCAccessor, secondCCAccessor, level_1)){
                                isProved = true;
                            }
                        }else{
                            if(reflexivityBingoTest(secondCCAccessor, firstCCAccessor, level_1)){
                                isProved = true;
                            }
                        }

                    }
            }
            //continue with the merge process
            if(!isProved) {
                mergeClasses(firstCCAccessor, secondCCAccessor);
            }
        }
    }

    //it works with the dominant classes already, so it takes in any class designator.
    public boolean areCongruent(int firstAccessor, int secondAccessor){
        return areClassesCongruent(firstAccessor,secondAccessor);
    }



    //The following operations are not yet specified in the registry template, but hey are necessary for the congruence class registry to work
    public boolean checkIfProved(){
        return isProved;
    }

    public void updateClassAttributes(int classAccessor, BitSet attributeIn){
        BitSet attributeAt = new BitSet();
        attributeAt = congruenceClassArray[classAccessor].getAttribute();
        attributeAt.or(attributeIn);
        congruenceClassArray[classAccessor].setClassAttribute(attributeAt);
        if(isSequentVCProvedByAttributes(attributeAt)){
            isProved = true;
        }
    }

    public void addOperatorToSuccedentReflexiveOperatorSet(Integer treeNodeLabel){
        succedentReflexiveOperatorsSet.add(treeNodeLabel);
    }



/************************************* PRIVATE METHODS ****************************************************************************/

    /**
     *<p>The operation that gets the ultimate dominant class designator for a given class</p>
     * @param cClassDesingator is a current class designator.
     * @return int value for the ultimate class designator for the provided designator.
     */
    private int getTheUltimateDominantClass(int cClassDesingator){
        while (congruenceClassArray[cClassDesingator].getDominantCClass() != cClassDesingator){
            cClassDesingator = congruenceClassArray[cClassDesingator].getDominantCClass();
        }
        return cClassDesingator;
    }

    /**
     * <p>{@code areClassesCongruent} checks to see of two classes are congruent</p>
     *
     * @param firstAccessor is an accessor for the first class provided by the client.
     * @param secondAccessor is an accessor for the second class provided by the client
     * @return {@code true} iff the two provided classes are congruent. Otherwise, return {@code false}.
     */
    private boolean areClassesCongruent(int firstAccessor, int secondAccessor){
        if (firstAccessor == secondAccessor){
            return true;
        }else{//one is the dominant of each other, or both have the same dominant class
            int dominantFirstClass = getTheUltimateDominantClass(firstAccessor);

            int dominantSecondClass = getTheUltimateDominantClass(secondAccessor);

            if(dominantFirstClass == dominantSecondClass){
                return true;
            }


        }
        return false;
    }


    /**
     * <p>This is a sub-procedure for the reflexivity Bingo test {@code reflexivityBingoTest}</p>
     *
     * @param indexToArgInSecondLevel index to the argument string representing first occurrence of the class in second level.
     * @param classAccessorInFirstLevel accessor for the class in the first level that the second class should match.
     * @return {@code true} iff the previous class from {@param indexToArgInSecondLevel} is {@param classAccessorInFirstLevel}, and
     * the root node of the cluster is in the set of reflexive operators. Otherwise return {@code false}.
     *
     */
    private boolean subReflexiveBingoTest(int indexToArgInSecondLevel, int classAccessorInFirstLevel){

    int currentCluster;
    BitSet multiplier = new BitSet();
    BitSet classAttributes = new BitSet();
    //this check is from the second level looking the second argument upwards
    //moving from one 7 to another 7 in the same level to see if we hit the right 7 that has been changed to a 3 and previous is 3
    while (clusterArgumentArray[indexToArgInSecondLevel].getNxtIndexWithSameCCNumberInLevel() != indexToArgInSecondLevel) {

        if (clusterArgumentArray[clusterArgumentArray[indexToArgInSecondLevel].getPrevClusterArg()].getCcNumber() == classAccessorInFirstLevel) {
            currentCluster = clusterArgumentArray[indexToArgInSecondLevel].getClusterNumber(); //first class
            while(clusterArray[currentCluster].getNextWithSameArg() != currentCluster) {
                //check the cluster if it has the operator we are looking for
                classAttributes = congruenceClassArray[clusterArray[currentCluster].getIndexToCongruenceClass()].getAttribute();
                multiplier.set(2);
                classAttributes.and(multiplier);
                if (succedentReflexiveOperatorsSet.contains(clusterArray[currentCluster].getTreeNodeLabel()) && classAttributes.cardinality() == 1) {
                    //we may need the classes to be dominant
                    return true;
                }
                //get to the next cluster that uses the same argument
                currentCluster = clusterArray[currentCluster].getNextWithSameArg();
            }
        }
        //get the next 7 in the lavel 2, it might have been another seven preceded with 3
        indexToArgInSecondLevel = clusterArgumentArray[indexToArgInSecondLevel].getNxtIndexWithSameCCNumberInLevel();
        //currentCluster = clusterArgumentArray[indexToArgString_7].getClusterNumber();
    }
    return false;
}

    /**
     * <p>This is one way to check if the sequent VC is proved. It is called when there is an existence of the reflexive operator in the
     * succedent.</p>
     *
     * @param firstClassAccessor is accessor for the first class, {@param firstClassAccessor} is considered smaller than {@param secondClassAccessor}.
     * @param secondClassAccessor is accessor for the second class, {@param secondClassAccessor} is considered greater than {@param firstClassAccessor}.
     * @param level is the level in the argument string array for the second class.
     * @return {@code true} iff the sequent VC is proved, otherwise it returns {@code false}
     */
    private boolean reflexivityBingoTest(int firstClassAccessor, int secondClassAccessor, int level ){

        if(level == 2) {
            int indexToArgString_7 = congruenceClassArray[secondClassAccessor].getIndexInClusterArgArrayFromASOP(2);
            return subReflexiveBingoTest(indexToArgString_7, firstClassAccessor);

        }else if (level == 1) {

            int indexToArgString_3 = congruenceClassArray[firstClassAccessor].getIndexInClusterArgArrayFromASOP(2);
            return subReflexiveBingoTest(indexToArgString_3, secondClassAccessor);

        }
        return false;
    }



    /**
     * <p>The operation merges two attributes for two merged classes into one. The attribute on the class with larger accessor is merged
     * into the attribute on the class with smaller accessor</p>
     *
     * @param firstAccessor is accessor for the first class, {@param firstAccessor} is considered smaller than {@param secondAccessor}.
     * @param secondAccessor is accessor for the second class, {@param secondAccessor} is considered greater than {@param firstAccessor}.
     * @return BitSet resulting from merging the two individual bit sets for the first class and second class.
     */
    private BitSet mergeAttribute(int firstAccessor, int secondAccessor){
        //just as example, first accessor = 3, second accessor = 7
        BitSet bitSet_03 = congruenceClassArray[firstAccessor].getAttribute();
        BitSet bitSet_07 = congruenceClassArray[secondAccessor].getAttribute();
        bitSet_03.or(bitSet_07); //OR the two bit sets and the result will be in bitSet_03.
        return bitSet_03;
    }


    /**
     * <p> This operation checks whether the sequent VC is proved or not using the BitSet on the classes </p>
     *
     * @param bitSetIn is a bitset provided to be checked if it proves the sequent VC.
     * @return {@code true} iff the {@param bitSeIn} has all the bits set, which proves the sequent VC. Otherwise, the operation returns {@code false}.
     */
    private boolean isSequentVCProvedByAttributes(BitSet bitSetIn){
        // the number of 1's is 3 when all three bits are set.
        if(bitSetIn.cardinality() == 3){
            return true;
        }
        return false;
    }


    /**
     * <p>The operation adds a new plantation to the variety list designated by {@param treeNodeLabel}. It starts a new list when the list is not existing
     * or join to the existing list. The plantations are kept in ascending order according to their indices in {@code plantationArray}</p>
     *
     * @param treeNodeLabel a designator for the variety
     * @param newPlantation a new plantation to be included into the variety list for the {@param treeNodeLabel}
     * @param plantationTag a plantation tag for the {@param newPlantation}
     */
    private void addInVarietyListArray(Integer treeNodeLabel, int newPlantation, int plantationTag){
        int currentPlantationInVarietyList = 0;
        if(varietyArray[treeNodeLabel] == null){
            VarietyList varietyList = new VarietyList(newPlantation,plantationTag);
            varietyArray[treeNodeLabel] = varietyList;
        }else{
            currentPlantationInVarietyList = varietyArray[treeNodeLabel].getFirstPlantation();
            if(newPlantation < currentPlantationInVarietyList ){ // put it at the front
                plantationArray[newPlantation].setNextVrtyPlantation(currentPlantationInVarietyList); //set the next plantation on the new plantation
                plantationArray[currentPlantationInVarietyList].setPrvVrtyPlantation(newPlantation);
                varietyArray[treeNodeLabel].setFirstPlantation(newPlantation); //update the fist plantation in the variety list in array
            } else { //put it at the end of the list or somewhere suitable according to the order
                while ( newPlantation > currentPlantationInVarietyList && plantationArray[currentPlantationInVarietyList].getNextVrtyPlantation() != 0) {
                    currentPlantationInVarietyList = plantationArray[currentPlantationInVarietyList].getNextVrtyPlantation();
                }
                plantationArray[currentPlantationInVarietyList].setNextVrtyPlantation(newPlantation);
                plantationArray[newPlantation].setPrvVrtyPlantation(currentPlantationInVarietyList);
            }

        }
    }

    /**
     * <p>The operation merges the two classes in the registry accessed by {@param firstCCAccessor} and {@param secondCCAccessor}
     * the class with large accessor is merged to the one with smaller accessor. The new class containing the content of the two
     * classes is designated with the smaller designator, and the dominant class for the larger designator is updated to the
     * smaller designator </p>
     * @param firstCCAccessor is an accessor for the first class provided by the client.
     * @param secondCCAccessor is accessor for the second class provided by the client.
     */
    private void mergeClasses(int firstCCAccessor, int secondCCAccessor){
        //update dominant class and perform path compression upward the chain
        if(firstCCAccessor < secondCCAccessor ){
            updateDominantClass(firstCCAccessor, secondCCAccessor); //just change the dominant class for the larger class designator
            updatePlantationInSmallerClass(firstCCAccessor, secondCCAccessor); //now update the plantation either by joining their clusters or moving the plantation to the smaller class
        }else{
            updateDominantClass(secondCCAccessor, firstCCAccessor); //just change the dominant class for the larger class designator
            updatePlantationInSmallerClass(secondCCAccessor, firstCCAccessor);//now update the plantation either by joining their clusters or moving the plantation to the smaller class
        }
        int level = 0;
        //take the second accessor and find where we should start looking in the arg string, get the level and index in FASOP
        if(firstCCAccessor < secondCCAccessor) {
            level = congruenceClassArray[secondCCAccessor].getLastArgStringPosition();
        }else{
            level = congruenceClassArray[firstCCAccessor].getLastArgStringPosition();
        }
        while(level != 0){
            //the order matter for updateClusterArgumentAfterMerging operation, see the operation signature around line 584
            if(firstCCAccessor < secondCCAccessor) {
                if(!isProved) { //update only when it is not proved
                    updateClusterArgumentAfterMerging(firstCCAccessor, secondCCAccessor, level);
                }else {
                    break;
                }
            }else{
                if(!isProved) {
                    updateClusterArgumentAfterMerging(secondCCAccessor, firstCCAccessor, level);
                }else {
                    break;
                }
            }
            level--;
        }

    }

    //make the smaller class the dominant class, and perform path compression
    //assumes firstCCAccessor is smaller than secondCCAccessor always
    //first accessor = 3, second accessor = 7, just as example

    /**
     *<p>The operation updates the dominant class designator for the {@param secondCCAccessor} to point to the {@param firstCCAccessor} ultimate designator.
     * The attributes are also updated, where {@param firstCCAccessor} will now have an attribute resulting from merging the two individual attributes </p>
     *
     * @param firstCCAccessor is accessor for the first class, {@param firstCCAccessor} is considered smaller than {@param secondCCAccessor}.
     * @param secondCCAccessor is accessor for the second class, {@param secondCCAccessor} is considered greater than {@param firstCCAccessor}.
     */
    private void updateDominantClass(int firstCCAccessor, int secondCCAccessor){
        if (congruenceClassArray[firstCCAccessor].getDominantCClass() == firstCCAccessor){ //it is its own dominant class so no compression
            congruenceClassArray[secondCCAccessor].setDominantCClass(firstCCAccessor);
            //update the attribute too at class level, which it is dependant of the firstCCAccessor which has to be smaller
            BitSet bitSet_03 = mergeAttribute(firstCCAccessor, secondCCAccessor);
            congruenceClassArray[firstCCAccessor].setClassAttribute(bitSet_03); //update the bit set for class 3, it is now containing new stuff from class 7

        }else { // it is not, compression needed
            int currentDominantClass = firstCCAccessor;
            //go up the chain as far as possible, I did not consider going down the chain
            currentDominantClass = getTheUltimateDominantClass(currentDominantClass);

            congruenceClassArray[secondCCAccessor].setDominantCClass(currentDominantClass);

            //update the attribute too at class level
            BitSet bitSet_03 = mergeAttribute(currentDominantClass, secondCCAccessor);
            //update the bit set for currentDominantClass, it is now containing new stuff from class 7
            congruenceClassArray[currentDominantClass].setClassAttribute(bitSet_03);
        }
    }

    //remove a class that its plantations have been moved after merge from the variety list
    //This will be for each label, just remove the respective plantation from the list of plantations
    /**
     *<p>The operation removes the class in the variety list after the plantation that stood in that class for a root label is merged
     * to another class</p>
     * @param treeNodeLabel the tree node label designating the plantation
     * @param plantationDesignatorToRemove index to the plantation that is to be removed
     */
    private void removeClassFromVarietyList(Integer treeNodeLabel, int plantationDesignatorToRemove){
        int currentPlantationInList = varietyArray[treeNodeLabel].getFirstPlantation();
        int previousPlantationInList = 0;
        int nextPlantationInList = 0;
        if(currentPlantationInList == plantationDesignatorToRemove){//it is the first one in the variety array list, now it has to be removed
            //just get rid of the first one and make the second one in the variety list the first one
            varietyArray[treeNodeLabel].setFirstPlantation(plantationArray[varietyArray[treeNodeLabel].getFirstPlantation()].getNextVrtyPlantation());
            //make the previous pointer 0
            plantationArray[plantationArray[varietyArray[treeNodeLabel].getFirstPlantation()].getNextVrtyPlantation()].setPrvVrtyPlantation(0);
        }else{ //it is not the first one in the variety array list, just remove it
            //this assumes plantation designator to remove must be in the variety list. If that is the case just remove it by re-allocating the pointers
            previousPlantationInList = plantationArray[plantationDesignatorToRemove].getPrvVrtyPlantation();
            nextPlantationInList = plantationArray[plantationDesignatorToRemove].getNextVrtyPlantation();

            plantationArray[previousPlantationInList].setNextVrtyPlantation(nextPlantationInList);
            if(nextPlantationInList != 0) { //note we have P0 as the initial plantation in
                plantationArray[nextPlantationInList].setPrvVrtyPlantation(previousPlantationInList);
            }
        }
    }

    //precondition the method assumes firstCCAccessor is smaller, note that when calling
    //precondition here is the tree node labels are converted to integers, some structure should deal with that
    //precondition here is everything is sorted in the plantation list, and cluster list, class list. TODO make sure everything is sorted in ascending order
    /**
     *<p>The operation update the plantations in the smaller class after the two classes are merged</p>
     * @param firstCCAccessor the smaller class designator
     * @param secondCCAccessor the larger class designator
     */
    private void updatePlantationInSmallerClass(int firstCCAccessor, int secondCCAccessor){
        int currentFirstAccessor = firstCCAccessor;
        int currentSecondAccessor = secondCCAccessor;

        int plantationDesignator_1 = congruenceClassArray[currentFirstAccessor].getFirstPlantation();
        int plantationDesignator_2 = congruenceClassArray[currentSecondAccessor].getFirstPlantation();

        Integer treeNodeLabel_2, treeNodeLabel_1; // tree node label 2 is the one for the plantation being moved
        int nextPlantationDesignator_1,nextPlantationDesignator_2;
        //plantationArray[plantationDesignator_2].getNextCCPlantation() != plantationDesignator_2
        while(plantationDesignator_2 != 0) {

            treeNodeLabel_2 = plantationArray[plantationDesignator_2].getTreeNodeLabel(); //get the tree node label for this plantation
            treeNodeLabel_1 = plantationArray[plantationDesignator_1].getTreeNodeLabel();

            //things may be changed and re-arranged, keep this record and use it later
            nextPlantationDesignator_1 = plantationArray[plantationDesignator_1].getNextCCPlantation();
            nextPlantationDesignator_2 = plantationArray[plantationDesignator_2].getNextCCPlantation();

            //compare the tree node labels and do what is necessary

            if (treeNodeLabel_2.equals(treeNodeLabel_1)) { // the tree nodes for the plantations are the same, join the clusters
                joinClustersOnSameRootNodePlantation(plantationDesignator_1, plantationDesignator_2);
                // update the variety list in the variety array
                removeClassFromVarietyList(treeNodeLabel_2,plantationDesignator_2);
                plantationDesignator_2 = nextPlantationDesignator_2;
                plantationDesignator_1 = nextPlantationDesignator_1;

            }else if(treeNodeLabel_1 < treeNodeLabel_2){ // this means we still need to check if the next in list one is still smaller than treeNodeLabel_2
                //this is teh case we can still find equality
                //plantationDesignator_1 = nextPlantationDesignator_1;
                joinPlantationFrom2ndListToFirstList(plantationDesignator_1, plantationDesignator_2);
                plantationDesignator_1 = plantationDesignator_2;//lets start from the plantation added to the first list
                plantationDesignator_2 = nextPlantationDesignator_2; // lets start from the next one on the second list

            }else { // treeNodeLabel_1 > treeNodeLabel_2 so it is  definitely not on the list, just merge merge the plantations
                //I think this condition will only happen once, as the rest will be greater than what we just added from list two
                joinPlantationFrom2ndListToFirstList(plantationDesignator_1, plantationDesignator_2);
                plantationDesignator_1 = plantationDesignator_2; //lets start from the plantation added to the first list

                //update the 1st plantation in the first class, this is assuming the idea that this part will only be executed once.
                congruenceClassArray[firstCCAccessor].setFirstPlantation(plantationDesignator_2);

                plantationDesignator_2 = nextPlantationDesignator_2; //lets start from the next one on the second list
            }


        }
    }



    //plantation designators provided here have different root node label and so we move the plantation on the second list to the fist list while maintaining the order
    //TODO this might be unnecessary helper function, get rid of it and move what necessary above

    /**
     * <p>The operation move a plantation from the larger class to the smaller class where it will be merged to the new list
     * according to their root node label. In this case, the operation covers the merger when the root nodes are either
     * greater than or less than than each other </p>
     * @param plantationDesignator_1 index to the first plantation in the first class
     * @param plantationDesignator_2 index to the second plantation in the second class
     *
     */
    private void joinPlantationFrom2ndListToFirstList(int plantationDesignator_1, int plantationDesignator_2){
        //deal with tree node labels 
        Integer treeNodeLabel_1 = plantationArray[plantationDesignator_1].getTreeNodeLabel();
        Integer treeNodeLabel_2 = plantationArray[plantationDesignator_2].getTreeNodeLabel();

        if(treeNodeLabel_1 < treeNodeLabel_2){
            plantationJoinCase_01(plantationDesignator_1, plantationDesignator_2);
        }else{
            plantationJoinCase_02(plantationDesignator_1, plantationDesignator_2);
        }
    }

    //plantation designators provided here have the same root node label and so we join their clusters

    /**
     *<p>The operation join clusters in plantations that have the same root node label by moving the clusters from the
     * larger plantation to the smaller plantation</p>
     * @param plantationDesignator_1 index to the smaller plantation
     * @param plantationDesignator_2 index to the larger plantation
     */
    private void joinClustersOnSameRootNodePlantation(int plantationDesignator_1, int plantationDesignator_2){
        int currentClusterDesignator_1 = plantationArray[plantationDesignator_1].getFirstPlantationCluster();
        int currentClusterDesignator_2 = plantationArray[plantationDesignator_2].getFirstPlantationCluster();

        int reserveCurrentPlantationCluster_1 = currentClusterDesignator_1;
        int reserveCurrentPlantationCluster_2 = currentClusterDesignator_2;
        int tempCurrentClusterDesignator_2 = 0;

        //update all clusters in the plantation to belong to the new class by changing their class field
        //we are using the dominant class, and we do this before the merging of clusters
        int dominantClassDesignator = getTheUltimateDominantClass(congruenceClassArray[clusterArray[reserveCurrentPlantationCluster_1].getIndexToCongruenceClass()].getDominantCClass());
        //This condition should work if the cluster at index 0 has all 0's. This is done in line 66
        while( clusterArray[reserveCurrentPlantationCluster_2].getNextPlantationCluster() != reserveCurrentPlantationCluster_2){
            clusterArray[reserveCurrentPlantationCluster_2].setIndexToCongruenceClass(dominantClassDesignator);
            reserveCurrentPlantationCluster_2 = clusterArray[reserveCurrentPlantationCluster_2].getNextPlantationCluster();
        }

        //This condition will work if the cluster array at index 0 is filled with all zeroes. This is done in line 66
        //the list next plantation cluster is not ordered by root node label as next with same argument
        while(clusterArray[currentClusterDesignator_2].getNextPlantationCluster() != currentClusterDesignator_2){ //until we get all clusters in the larger plantation
            if(currentClusterDesignator_1 < currentClusterDesignator_2) {
                tempCurrentClusterDesignator_2 = currentClusterDesignator_2;
                //currentClusterDesignator_1 = currentClusterDesignator_2;
                currentClusterDesignator_2 = clusterJoinCase_01(currentClusterDesignator_1, currentClusterDesignator_2); //get what next on list_2
                currentClusterDesignator_1 = tempCurrentClusterDesignator_2; //let's start from where we added the cluster in list_1
            }else{
                tempCurrentClusterDesignator_2 = currentClusterDesignator_2;
                //currentClusterDesignator_1 = currentClusterDesignator_2;
                currentClusterDesignator_2 = clusterJoinCase_02(currentClusterDesignator_1,currentClusterDesignator_2);//get what is next from list_2
                currentClusterDesignator_1 = tempCurrentClusterDesignator_2; //let's start from where we added the cluster in list_1
                //under the assumption this will only be executed once and that will now be our fist cluster in the list_1
                plantationArray[plantationDesignator_1].setFirstPlantationCluster(currentClusterDesignator_1);
            }
        }
    }


    //this is the case when cluster on the smaller plantation is less than the cluster on the larger plantation
    //called with currentClusterDesignator_1 < currentClusterDesignator_2, currentClusterDesignator_2 is a newcomer

    /**
     * <p>This operation mergers two clusters one for the smaller plantation and one from the larger plantation, in this case,
     * the cluster on the smaller plantation is less than the cluster on the larger plantation</p>
     * @param currentClusterDesignator_1 is the class designator for the first cluster in smaller plantation
     * @param currentClusterDesignator_2 is the class designator for the second cluster in a larger plantation
     * @return a cluster designator for the cluster just moved to the smaller plantation
     */
    private int clusterJoinCase_01(int currentClusterDesignator_1, int currentClusterDesignator_2){
        int next_1, next_2, prev_2;

        while(currentClusterDesignator_1 < currentClusterDesignator_2 && clusterArray[currentClusterDesignator_1].getNextPlantationCluster() != 0 && clusterArray[currentClusterDesignator_1].getNextPlantationCluster() < currentClusterDesignator_2){
            currentClusterDesignator_1 = clusterArray[currentClusterDesignator_1].getNextPlantationCluster();
        }

        //keep records of all pointers
        next_1 = clusterArray[currentClusterDesignator_1].getNextPlantationCluster();
        next_2 = clusterArray[currentClusterDesignator_2].getNextPlantationCluster();
        prev_2 = clusterArray[currentClusterDesignator_2].getPreviousPlantationCluster();

        //completely restore everything, every prev and next to something that can be worked on from scratch, I guess it will solve the problem in a more general way
        clusterArray[currentClusterDesignator_1].setNextPlantationCluster(currentClusterDesignator_2);

        clusterArray[currentClusterDesignator_2].setPreviousPlantationCluster(currentClusterDesignator_1);
        clusterArray[currentClusterDesignator_2].setNextPlantationCluster(next_1);
        clusterArray[next_1].setPreviousPlantationCluster(currentClusterDesignator_2);

        clusterArray[next_2].setPreviousPlantationCluster(prev_2);

        //update the class designator field in the cluster to pointer to the new class they belong
        clusterArray[currentClusterDesignator_2].setIndexToCongruenceClass(clusterArray[currentClusterDesignator_1].getIndexToCongruenceClass());


        return next_2; //return where to start on list_2
    }

    //this case is when plantationDesignator_1 < plantationDesignator_2, plantationDesignator_2 is a newcomer

    /**
     *
     *<p>The operation join two plantations where the tree node designating the first one is less than the tree node designating
     * the second one </p>
     * @param plantationDesignator_1 index to plantation array for the first plantation
     * @param plantationDesignator_2 index to plantation orray for the second plantation
     */
    private void plantationJoinCase_01(int plantationDesignator_1, int plantationDesignator_2){
        int next_1, currentPlantationCluster_2, currentPlantationCluster_1, dominantClassDesignator;

        //sort them using tree node labels
        Integer treeNodeLabel_1, treeNodeLabel_2;
        treeNodeLabel_1 = plantationArray[plantationDesignator_1].getTreeNodeLabel();
        treeNodeLabel_2 = plantationArray[plantationDesignator_2].getTreeNodeLabel();

        while(treeNodeLabel_1 < treeNodeLabel_2 && plantationArray[plantationDesignator_1].getNextCCPlantation() != 0 && plantationArray[plantationArray[plantationDesignator_1].getNextCCPlantation()].getTreeNodeLabel() < treeNodeLabel_2){
            plantationDesignator_1 = plantationArray[plantationDesignator_1].getNextCCPlantation();
            treeNodeLabel_1 = plantationArray[plantationDesignator_1].getTreeNodeLabel();
        }
        if (treeNodeLabel_2.equals(treeNodeLabel_1)) { // the tree nodes for the plantations are the same, join the clusters
            joinClustersOnSameRootNodePlantation(plantationDesignator_1, plantationDesignator_2);
        }else {
            //keep records of all pointers
            next_1 = plantationArray[plantationDesignator_1].getNextCCPlantation();

            plantationArray[plantationDesignator_1].setNextCCPlantation(plantationDesignator_2);
            plantationArray[plantationDesignator_2].setNextCCPlantation(next_1);

            //update all clusters in the plantation to belong to the new class by changing their class field
            currentPlantationCluster_2 = plantationArray[plantationDesignator_2].getFirstPlantationCluster();
            currentPlantationCluster_1 = plantationArray[plantationDesignator_1].getFirstPlantationCluster();
            //we are using the dominant class
            dominantClassDesignator = getTheUltimateDominantClass(congruenceClassArray[clusterArray[currentPlantationCluster_1].getIndexToCongruenceClass()].getDominantCClass());
            //This condition should work if the cluster at index 0 has all 0's. This is done in line 66
            while( clusterArray[currentPlantationCluster_2].getNextPlantationCluster() != currentPlantationCluster_2){
                clusterArray[currentPlantationCluster_2].setIndexToCongruenceClass(dominantClassDesignator);
                currentPlantationCluster_2 = clusterArray[currentPlantationCluster_2].getNextPlantationCluster();
            }
        }
    }

    //this is the case when cluster on the smaller plantation is greater than the cluster on the larger plantation
    //called with currentClusterDesignator_1 > currentClusterDesignator_2
    //similarly I think this method will be called only once, and therefore we should update the first cluster for the plantation
    /**
     * <p>This operation mergers two clusters one for the smaller plantation and one from the larger plantation, in this case,
     * the cluster on the smaller plantation is greater than the cluster on the larger plantation</p>
     * @param currentClusterDesignator_1 is the class designator for the first cluster in smaller plantation
     * @param currentClusterDesignator_2 is the class designator for the second cluster in a larger plantation
     * @return a cluster designator for the cluster moved to the smaller plantation
     */
    private int clusterJoinCase_02(int currentClusterDesignator_1, int currentClusterDesignator_2){
        int next_2, prev_2;

        //this loop will possibly not be executed, think more if so delete it
        while (currentClusterDesignator_1 > currentClusterDesignator_2 && clusterArray[currentClusterDesignator_1].getPreviousPlantationCluster() != 0){
            currentClusterDesignator_1 = clusterArray[currentClusterDesignator_1].getPreviousPlantationCluster();
        }

        //keep records of pointers
        next_2 = clusterArray[currentClusterDesignator_2].getNextPlantationCluster();
        prev_2 = clusterArray[currentClusterDesignator_2].getPreviousPlantationCluster();


        clusterArray[currentClusterDesignator_1].setPreviousPlantationCluster(currentClusterDesignator_2);
        clusterArray[currentClusterDesignator_2].setNextPlantationCluster(currentClusterDesignator_1);

        clusterArray[next_2].setPreviousPlantationCluster(prev_2);

        //update the class designator field in the cluster to pointer to the new class they belong
        clusterArray[currentClusterDesignator_2].setIndexToCongruenceClass(clusterArray[currentClusterDesignator_1].getIndexToCongruenceClass());

        return next_2;
    }

    //this case is when treeNodeLabel_1 > treeNodeLabel_2,
    //I think this will only be called once

    /**
     * <p>The operation join two plantations where the tree node designating the first one is greater than the tree node designating
     * the second one </p>
     * @param plantationDesignator_1 index to plantation array for the first plantation
     * @param plantationDesignator_2 index to plantation orray for the second plantation
     */
    private void plantationJoinCase_02(int plantationDesignator_1, int plantationDesignator_2){
        int currentPlantationCluster_2, currentPlantationCluster_1, dominantClassDesignator;

        //plantationArray[plantationDesignator_1].setNextCCPlantation(plantationDesignator_2);
        plantationArray[plantationDesignator_2].setNextCCPlantation(plantationDesignator_1);

        //update all clusters in the plantation to belong to the new class by changing their class field
        currentPlantationCluster_2 = plantationArray[plantationDesignator_2].getFirstPlantationCluster();
        currentPlantationCluster_1 = plantationArray[plantationDesignator_1].getFirstPlantationCluster();
        //assign the dominant class
        dominantClassDesignator = getTheUltimateDominantClass(congruenceClassArray[clusterArray[currentPlantationCluster_1].getIndexToCongruenceClass()].getDominantCClass());
        while( clusterArray[currentPlantationCluster_2].getNextPlantationCluster() != 0){
            clusterArray[currentPlantationCluster_2].setIndexToCongruenceClass(dominantClassDesignator);
            currentPlantationCluster_2 = clusterArray[currentPlantationCluster_2].getNextPlantationCluster();
        }
        //the final update when next plantation cluster is 0
        clusterArray[currentPlantationCluster_2].setIndexToCongruenceClass(dominantClassDesignator);


    }


    //firstAccessor < secondAccessor
    //to the argument string array and update everything there
    //this may cause other classes to collapse, put them in a queue
    //first accessor = 3, second accessor = 7, just as example

    /**
     * <p>The operation updates the cluster argument array after merging two classes, after merging we may need to update
     * FASOP, rearrange arguments ond merge clusters </p>
     * @param firstAccessor congruence class accessor for the smaller class e.g., 3
     * @param secondAccessor congruence class accessor for the larger class e.g., 7
     * @param level the level in the cluster argument string
     */
    private void updateClusterArgumentAfterMerging(int firstAccessor, int secondAccessor, int level) {

        int indexToArgString = congruenceClassArray[secondAccessor].getIndexInClusterArgArrayFromASOP(level);
        int indexToArgString_3 = congruenceClassArray[firstAccessor].getIndexInClusterArgArrayFromASOP(level);
        int tempIndexToArgString;
        if(congruenceClassArray[firstAccessor].getIndexInClusterArgArrayFromASOP(level) == 0){ //3 is not in the level at all
            //there are more than one argument string of 7 in the level enter the while loop, or pass forward
            while(clusterArgumentArray[indexToArgString].getNxtIndexWithSameCCNumberInLevel() != indexToArgString){
                clusterArgumentArray[indexToArgString].setCcNumber(firstAccessor);

                updateClassFASOP(firstAccessor, level, indexToArgString);
                reArrangeArguments(indexToArgString);
                indexToArgString = clusterArgumentArray[indexToArgString].getNxtIndexWithSameCCNumberInLevel();
            }

                //there is no other argument string of 7 in the level and non is existing for 3 in the level or its the final 7 after the list
                //first change the class number in the argument record from 7 to 3
                clusterArgumentArray[indexToArgString].setCcNumber(firstAccessor);
                //now we should go to the FASOP for class 3 and update it as now 3 exists in the level
                updateClassFASOP(firstAccessor, level, indexToArgString);
                reArrangeArguments(indexToArgString);

        }else { //3 is existing in the level and can be anywhere, take this by looking at each father and its children

            while (indexToArgString != 0) { //no two 7s will be under same father
                tempIndexToArgString = clusterArgumentArray[indexToArgString].getNxtIndexWithSameCCNumberInLevel();
                //3 is together with considered 7 under the same father
                boolean checkIfUnderSameParent = areClassesUnderSameParentInArgArray(indexToArgString,firstAccessor, level);
                if (checkIfUnderSameParent) {//3 and 7 have the same parent
                    if(clusterArgumentArray[indexToArgString].getNextClusterArg() == 0){//but 3 has no children
                        //first change the class number in the argument record from 7 to 3
                        clusterArgumentArray[indexToArgString].setCcNumber(firstAccessor);

                        //now we should go to the FASOP for class 3 and update it as now we have two 3s existing under same father
                        updateClassFASOP(firstAccessor, level, indexToArgString);

                        //we make the 7 which is now 3 dormant by skipping it
                        int nextIndexToFollow = clusterArgumentArray[clusterArgumentArray[indexToArgString].getPrevClusterArg()].getNextClusterArg();
                        int prevIndexToFollow = 0;
                        //The if statement around while loop is a change after debugging TODO double
                        if(nextIndexToFollow != 0 ) {//if it is zero it means it is the only argument, and alternative argument is 0
                            while (nextIndexToFollow != indexToArgString) {
                                prevIndexToFollow = nextIndexToFollow;
                                nextIndexToFollow = clusterArgumentArray[nextIndexToFollow].getAlternativeArg();
                            }
                        }
                        if(prevIndexToFollow == 0){//it is the first one in the children
                            //make the second child first
                            clusterArgumentArray[clusterArgumentArray[indexToArgString].getPrevClusterArg()].setNextClusterArg(clusterArgumentArray[indexToArgString].getAlternativeArg());
                        }else{//it is in between children, just deal with next alternative arguments
                            clusterArgumentArray[prevIndexToFollow].setAlternativeArg(clusterArgumentArray[indexToArgString].getAlternativeArg());
                        }
                        //update the clusters by merging the two lists
                        mergeClusters(indexToArgString, indexToArgString_3);

                    }else {
                        //move what is under 7 to what is under 3
                        mergeSuffixTo(indexToArgString, indexToArgString_3);

                        //update the clusters by merging the two lists
                        mergeClusters(indexToArgString, indexToArgString_3);
                    }

                } else { //3 is not under the same father for the considered 7
                    //first change the class number in the argument record from 7 to 3
                    clusterArgumentArray[indexToArgString].setCcNumber(firstAccessor);
                    //now we should go to the FASOP for class 3 and update it as now 3 exists in the level
                    updateClassFASOP(firstAccessor, level, indexToArgString);
                    reArrangeArguments(indexToArgString);
                }
                //updated after debugging to the next line after commented one TODO double check
                //indexToArgString = clusterArgumentArray[indexToArgString].getNxtIndexWithSameCCNumberInLevel(); //get the next 7
                indexToArgString = tempIndexToArgString;
            }
        }
    }



    //indexToArgString is the index on 7 and indexToArgString_3 is the index on 3, order matters

    /**
     * <p>The operation updates the cluster argument string by merging the children of arguments with classes that got merged </p>
     * @param indexToArgString index to cluster argument array with a larger class e.g., 7
     * @param indexToArgString_3 index to cluster argument array with a smaller class e.g., 3
     */
    private void mergeSuffixTo(int indexToArgString, int indexToArgString_3){

        int currentArgumentToMove = clusterArgumentArray[indexToArgString].getNextClusterArg();
        int nextArgumentToMove = clusterArgumentArray[currentArgumentToMove].getAlternativeArg();
        int currentLargestArgumentInList_3 = clusterArgumentArray[indexToArgString_3].getNextClusterArg();
        int previousLargestArgumentInList_3 = 0;
        int nextLargestArgumentInList_3 = 0;

        //int nextLargestArgumentInList_3 = clusterArgumentArray[currentLargestArgumentInList_3].getAlternativeArg();

        int classDesignator_7 = clusterArgumentArray[currentArgumentToMove].getCcNumber();
        int classDesignator_3 = clusterArgumentArray[currentLargestArgumentInList_3].getCcNumber();

        //the condition works if the cluster argument string at position 0, has all zeroes. Done in line 70
        while (clusterArgumentArray[currentArgumentToMove].getAlternativeArg() != currentArgumentToMove){

            //case_01, moving argument under 7 that doesn't exist under 3
            if(classDesignator_7 > classDesignator_3){
                //reallocate pointers in 3
                clusterArgumentArray[indexToArgString_3].setNextClusterArg(currentArgumentToMove);
                clusterArgumentArray[currentArgumentToMove].setPrevClusterArg(indexToArgString_3);
                clusterArgumentArray[currentArgumentToMove].setAlternativeArg(currentLargestArgumentInList_3);

                //reset what to deal with on the next iteration
                currentLargestArgumentInList_3 = currentArgumentToMove;


            }else if(classDesignator_7 < classDesignator_3) {
                //int previousLargestArgumentInList_3 = 0;
                nextLargestArgumentInList_3 = currentLargestArgumentInList_3;
                while(classDesignator_7 < classDesignator_3){//it has to go into this loop at least once previousLargestArgumentInList_3 can't be 0
                    previousLargestArgumentInList_3 = nextLargestArgumentInList_3;
                    nextLargestArgumentInList_3 = clusterArgumentArray[nextLargestArgumentInList_3].getAlternativeArg();
                    classDesignator_3 = clusterArgumentArray[nextLargestArgumentInList_3].getCcNumber();
                }
                if(classDesignator_3 == classDesignator_7){
                    //cut to the chase, these two will be under same parent, second one adopted
                    //call merge suffix again on the two new arguments
                    mergeSuffixTo(currentArgumentToMove, nextLargestArgumentInList_3);

                    //update the clusters by merging the two lists
                    mergeClusters(currentArgumentToMove, nextLargestArgumentInList_3);

                    //get the next one under 3
                    currentLargestArgumentInList_3 = clusterArgumentArray[currentLargestArgumentInList_3].getAlternativeArg();
                }else{
                    //update things on 3 side
                    clusterArgumentArray[previousLargestArgumentInList_3].setAlternativeArg(currentArgumentToMove);
                    clusterArgumentArray[currentArgumentToMove].setPrevClusterArg(indexToArgString_3);
                    clusterArgumentArray[currentArgumentToMove].setAlternativeArg(nextLargestArgumentInList_3);

                    //reset what to deal with on the next iteration
                    currentLargestArgumentInList_3 = currentArgumentToMove;

                }
            }else{ //classDesignator_7 == classDesignator_3
                mergeSuffixTo(currentArgumentToMove, currentLargestArgumentInList_3);

                //update the clusters by merging the two lists
                mergeClusters(currentArgumentToMove, currentLargestArgumentInList_3);

                //get the next one under 3
                currentLargestArgumentInList_3 = clusterArgumentArray[currentLargestArgumentInList_3].getAlternativeArg();
            }
            //reallocate pointers in 7
            clusterArgumentArray[indexToArgString].setNextClusterArg(nextArgumentToMove);

            //reset what to deal with on the next iteration
           // currentLargestArgumentInList_3 = currentArgumentToMove;
            currentArgumentToMove = clusterArgumentArray[indexToArgString].getNextClusterArg();
            nextArgumentToMove = clusterArgumentArray[currentArgumentToMove].getAlternativeArg();

            //might be common for all cases move down
            classDesignator_7 = clusterArgumentArray[currentArgumentToMove].getCcNumber();
            classDesignator_3 = clusterArgumentArray[currentLargestArgumentInList_3].getCcNumber();
        }
        //forget about 7 which is now 3 and after all its children are moved to the 3
        int indexToParentOfArg_7 = clusterArgumentArray[indexToArgString].getPrevClusterArg();
        int nextAfter7 = clusterArgumentArray[indexToArgString].getAlternativeArg();
        clusterArgumentArray[indexToParentOfArg_7].setNextClusterArg(nextAfter7);

    }


    //the cluster that used to have separate argument now they have same argument after 7 turning 3
    //the cluster that used to have separate argument now they have same argument after having same child and father next level under

    /**
     * <p>The operation merges to two clusters that used to have different arguments and now they have the same argument after
     * the larger class turning to the smaller class e.g., 7 ---> 3 </p>
     * @param indexToArgString index to argument string for the larger class e.g., 7
     * @param indexToArgString_3 index to argument string for the smaller class e.g., 3
     */
    private void mergeClusters(int indexToArgString, int indexToArgString_3 ){

        int indexInClusterArray_7 = clusterArgumentArray[indexToArgString].getClusterNumber();
        int indexInClusterArray_3 = clusterArgumentArray[indexToArgString_3].getClusterNumber();

        int nextIndexInClusterArray_7 = 0;
        int nextIndexInClusterArray_3 = 0;
        Integer label_7 = clusterArray[indexInClusterArray_7].getTreeNodeLabel();
        Integer label_3 = clusterArray[indexInClusterArray_3].getTreeNodeLabel();

        while(indexInClusterArray_7 != 0){ //move until the next with same argument string is 0
            if(label_7 > label_3){ // the label for the 7 list is greater than the 3 list

                nextIndexInClusterArray_7 = clusterArray[indexInClusterArray_7].getNextWithSameArg();
                clusterArray[indexInClusterArray_7].setNextWithSameArg(indexInClusterArray_3);
                indexInClusterArray_3 = indexInClusterArray_7;
                indexInClusterArray_7 = nextIndexInClusterArray_7;
                label_7 = clusterArray[indexInClusterArray_7].getTreeNodeLabel();
                label_3 = clusterArray[indexInClusterArray_3].getTreeNodeLabel();
            }else if(label_7 < label_3){ // the label for the 7 list is smaller than the 7 list

                if(clusterArray[clusterArray[indexInClusterArray_3].getNextWithSameArg()].getTreeNodeLabel().equals(clusterArray[indexInClusterArray_7].getTreeNodeLabel())){
                    indexInClusterArray_3 = clusterArray[indexInClusterArray_3].getNextWithSameArg();
                    label_3 = clusterArray[indexInClusterArray_3].getTreeNodeLabel();
                }else {
                    nextIndexInClusterArray_7 = clusterArray[indexInClusterArray_7].getNextWithSameArg();
                    while (clusterArray[clusterArray[indexInClusterArray_3].getNextWithSameArg()].getTreeNodeLabel() > clusterArray[indexInClusterArray_7].getTreeNodeLabel()) {
                        indexInClusterArray_3 = clusterArray[indexInClusterArray_3].getNextWithSameArg();
                    }
                    nextIndexInClusterArray_3 = clusterArray[indexInClusterArray_3].getNextWithSameArg();
                    clusterArray[indexInClusterArray_3].setNextWithSameArg(indexInClusterArray_7);
                    clusterArray[indexInClusterArray_7].setNextWithSameArg(nextIndexInClusterArray_3);
                    indexInClusterArray_3 = indexInClusterArray_7;
                    indexInClusterArray_7 = nextIndexInClusterArray_7;
                    label_7 = clusterArray[indexInClusterArray_7].getTreeNodeLabel();
                    label_3 = clusterArray[indexInClusterArray_3].getTreeNodeLabel();
                }
            }else if(label_3.equals(label_7)){
                int dominantClass_3 = getTheUltimateDominantClass(congruenceClassArray[clusterArray[indexInClusterArray_3].getIndexToCongruenceClass()].getDominantCClass());
                int dominantClass_7 = getTheUltimateDominantClass(congruenceClassArray[clusterArray[indexInClusterArray_7].getIndexToCongruenceClass()].getDominantCClass());

                //gone back to the dominant class
                if(dominantClass_7 == dominantClass_3){
                    //NOTHING HAPPENS
                    //TODO: put unused cluster to the re-use list
                }else{
                    classMergeList.add(dominantClass_3);
                    classMergeList.add(dominantClass_7);
                }
                //change the dominant class of 7 to 3, which happens in both cases above
                clusterArray[indexInClusterArray_7].setDominantCluster(clusterArray[indexInClusterArray_3].getDominantCluster());

                // corrected after debugging TODO make sure it is right
                //get to the next one with same argument on list 7
                indexInClusterArray_7 = clusterArray[indexInClusterArray_7].getNextWithSameArg();
                indexInClusterArray_3 = clusterArray[indexInClusterArray_3].getNextWithSameArg();
                //get the new tree node labels
                label_7 = clusterArray[indexInClusterArray_7].getTreeNodeLabel();
                label_3 = clusterArray[indexInClusterArray_3].getTreeNodeLabel();
            }

        }

    }

    //rearrange starting from the index in arg string with changed class

    /**
     * <p>The operation rearrange children under a parent and keep them in order, children are ordered in descending order</p>
     * @param argIndexWithChangedClass index to the argument string of the class that got changed e.g., 7 ---> 3
     */
    private void reArrangeArguments(int argIndexWithChangedClass){

        //It is the only child under a parent, or the last one of the children,
        //No rearrangement needed
        if (clusterArgumentArray[argIndexWithChangedClass].getAlternativeArg() == 0){
        }else{
            //changed class has siblings under the parent, some shifting will happen in different cases below
            if(clusterArgumentArray[clusterArgumentArray[argIndexWithChangedClass].getPrevClusterArg()].getNextClusterArg() == argIndexWithChangedClass){ //was it the first one(largest in the list)? if so we have to deal with pointers in the parent
                //make the parent point to the next after cluster record that we have changed to a lower class
                clusterArgumentArray[clusterArgumentArray[argIndexWithChangedClass].getPrevClusterArg()].setNextClusterArg(clusterArgumentArray[argIndexWithChangedClass].getAlternativeArg());

                //move it to the right place
                int previousIndexToArgString = argIndexWithChangedClass;
                int nextIndexToArgString = clusterArgumentArray[argIndexWithChangedClass].getAlternativeArg();
                while(clusterArgumentArray[argIndexWithChangedClass].getCcNumber() < clusterArgumentArray[nextIndexToArgString].getCcNumber() ){
                    previousIndexToArgString = nextIndexToArgString;
                    nextIndexToArgString = clusterArgumentArray[nextIndexToArgString].getAlternativeArg();
                }
                //set our changed argument record to have an alternative argument less than it
                clusterArgumentArray[argIndexWithChangedClass].setAlternativeArg(nextIndexToArgString);

                //the previous one should now point to our changed argument record
                clusterArgumentArray[previousIndexToArgString].setAlternativeArg(argIndexWithChangedClass);

            }else{ //it is not the first child
                int currentIndexToArgString = clusterArgumentArray[clusterArgumentArray[argIndexWithChangedClass].getPrevClusterArg()].getNextClusterArg();
                int nextIndexToArgString = clusterArgumentArray[currentIndexToArgString].getAlternativeArg();
                int previousIndexToArgString = currentIndexToArgString;

                    //lets walk the list of children until we get to the changed child
                    while (nextIndexToArgString != argIndexWithChangedClass){
                        previousIndexToArgString = nextIndexToArgString;
                        nextIndexToArgString = clusterArgumentArray[nextIndexToArgString].getAlternativeArg();
                    }

                    //connect the two left and right children before we remove the middle one to its rightful place
                    clusterArgumentArray[previousIndexToArgString].setAlternativeArg(clusterArgumentArray[argIndexWithChangedClass].getAlternativeArg());

                    previousIndexToArgString = argIndexWithChangedClass;
                    nextIndexToArgString = clusterArgumentArray[argIndexWithChangedClass].getAlternativeArg();

                    while(clusterArgumentArray[argIndexWithChangedClass].getCcNumber() < clusterArgumentArray[nextIndexToArgString].getCcNumber() ){
                        previousIndexToArgString = nextIndexToArgString;
                        nextIndexToArgString = clusterArgumentArray[nextIndexToArgString].getAlternativeArg();
                    }
                    //set our changed argument record to have an alternative argument less than it
                    clusterArgumentArray[argIndexWithChangedClass].setAlternativeArg(nextIndexToArgString);

                    //the previous one should now point to our changed argument record
                    clusterArgumentArray[previousIndexToArgString].setAlternativeArg(argIndexWithChangedClass);
                }
            }
    }


    /**
     * <p>The operation checks if the two classes on the argument list are under the same parent</p>
     * @param indexToString index to the argument string with larger class e.g., 7
     * @param firstAccessor smaller class accessor e.g., 3
     * @param level the argument string level
     * @return the operation returns true when a parent for the argument at
     */
    private boolean areClassesUnderSameParentInArgArray(int indexToString, int firstAccessor, int level){
        int parent_07 = clusterArgumentArray[indexToString].getPrevClusterArg();
        int indexToFollow = congruenceClassArray[firstAccessor].getIndexInClusterArgArrayFromASOP(level);
        int parent_03 = clusterArgumentArray[indexToFollow].getPrevClusterArg();

        if(parent_07 == parent_03){
            return true;
        }else if(parent_07 > parent_03){
            return false;
        }else{
            while(parent_07 < parent_03){
                indexToFollow = clusterArgumentArray[indexToFollow].getNxtIndexWithSameCCNumberInLevel();
                parent_03 = clusterArgumentArray[indexToFollow].getPrevClusterArg();
            }
            if(parent_07 == parent_03){
                return true;
            }
            return false;
        }

    }


    /**
     * <p>The operation create a cluster argument as an entry to the cluster argument array </p>
     * @param label a label to the respective cluster with arguments in {@param clusterArgumentString}
     * @param clusterArgumentString an argument string holding the arguments for the cluster with {@param label}
     * @return an integer value representing an index to the argument array for the argument created
     */
    private int createClusterArgumentArray(Integer label, Queue<Integer> clusterArgumentString){
        int index = 1; //initial index for the cluster argument array (CAA)
        int precedingIndex = 0;
        int indexInClusterArray = 0;
        int clusterNumber = 0;
        boolean existed = false;
        boolean alternativeExists = true;
        boolean precedingIndexUsed = false;

        //for ASOP
        int level = 0;

        if(argListLength(clusterArgumentString) == 0){ //for constants and variables
            if(clusterArgumentArray[index] == null){ //no arg of empty string has being created
                ClusterArgument clusterArgument = new ClusterArgument(0, 0, 0,1, 0);
                clusterArgumentArray[index] = clusterArgument;
            }else{ // empty arg string but already exists
                updateNextWithSameArgument(label, index, topCongruenceClusterDesignator);
            }
            return index;
        }else{
                while (argListLength(clusterArgumentString) > 0){ //arg string is not empty
                    int lastCCDesignator = removeFirstArgDesignator(); //remove the far right one first (FIFO)
                    if (clusterArgumentArray[index].getNextClusterArg() == 0 && clusterArgumentArray[index].getCcNumber() != lastCCDesignator){
                        if (argListLength(clusterArgumentString) == 0) clusterNumber = topCongruenceClusterDesignator;
                        ClusterArgument clusterArgument = new ClusterArgument(0, index, lastCCDesignator, clusterNumber, 0);
                        clusterArgumentArray[topArgStrArrIndex] = clusterArgument;
                        clusterArgumentArray[index].setNextClusterArg(topArgStrArrIndex); // old one to the new one
                        index = topArgStrArrIndex; //index now to the newly created argument array

                        //update ASOP for the class lastCCDesignator used here
                        updateClassFASOP(lastCCDesignator,++level,index);

                        topArgStrArrIndex++; //move to the next available
                    } else if(clusterArgumentArray[index].getNextClusterArg() == 0 && clusterArgumentArray[index].getCcNumber() == lastCCDesignator){
                        //we don't create a new arg string in the array
                        //But we have to update the cluster if it is the last argument in the current cluster arg string
                        if (argListLength(clusterArgumentString) == 0){
                            if(clusterArgumentArray[index].getClusterNumber() == 0){//it was created but it is not active yet
                                //set the cluster number to the one  created already
                                clusterArgumentArray[index].setClusterNumber(topCongruenceClusterDesignator);
                            }else { //it is an active string and we should go back and update previous cluster with similar arg
                                updateNextWithSameArgument(label, index, topCongruenceClusterDesignator);
                            }
                        }
                    }else if (clusterArgumentArray[index].getNextClusterArg() != 0 && clusterArgumentArray[index].getCcNumber() != lastCCDesignator){
                        index = clusterArgumentArray[index].getNextClusterArg();//change index to the next one
                        if(clusterArgumentArray[index].getCcNumber() == lastCCDesignator){
                            if (argListLength(clusterArgumentString) == 0) {
                                if(clusterArgumentArray[index].getClusterNumber() == 0){
                                    //set the cluster number to the one that will be created next
                                    clusterArgumentArray[index].setClusterNumber(topCongruenceClusterDesignator);
                                }else { //it is an active string and we should go back and update previous cluster with similar arg
                                    updateNextWithSameArgument(label, index, topCongruenceClusterDesignator);

                                }

                                return index;
                            }
                            level++; //even though we did not change anything in the argument string, we still have to increment the level
                        }else{
                            if(clusterArgumentArray[index].getAlternativeArg() == 0 && clusterArgumentArray[index].getCcNumber() < lastCCDesignator){
                                if (argListLength(clusterArgumentString) == 0) clusterNumber = topCongruenceClusterDesignator; // if it is the last arg string we are creating
                                ClusterArgument clusterArgument = new ClusterArgument(0,index - 1, lastCCDesignator, clusterNumber, index);
                                clusterArgumentArray[topArgStrArrIndex] = clusterArgument;
                                clusterArgumentArray[index - 1].setNextClusterArg(topArgStrArrIndex);
                                index = topArgStrArrIndex;

                                //update ASOP for the class lastCCDesignator used here
                                updateClassFASOP(lastCCDesignator,++level,index);

                                topArgStrArrIndex++;
                            }
                            if(clusterArgumentArray[index].getAlternativeArg() == 0 && clusterArgumentArray[index].getCcNumber() > lastCCDesignator){
                                if (argListLength(clusterArgumentString) == 0) clusterNumber = topCongruenceClusterDesignator; // if it is the last arg string we are creating
                                ClusterArgument clusterArgument = new ClusterArgument(0,index - 1, lastCCDesignator, clusterNumber, 0);
                                //clusterArgumentArray[index].setAlternativeArg(index + 1); //this doesn't do the right thing see next line
                                clusterArgumentArray[index].setAlternativeArg(topArgStrArrIndex); //seems to do the right thing but I have to check if it doesn't break anything else
                                clusterArgumentArray[topArgStrArrIndex] = clusterArgument;
                                index = topArgStrArrIndex;

                                //update ASOP for the class lastCCDesignator used here
                                updateClassFASOP(lastCCDesignator,++level,index);

                                topArgStrArrIndex++;
                            }
                            while (alternativeExists){
                                if (clusterArgumentArray[index].getAlternativeArg() != 0) {
                                    if (clusterArgumentArray[index].getCcNumber() == lastCCDesignator) {
                                        existed = true;
                                        break;
                                    } else {
                                        index = clusterArgumentArray[index].getAlternativeArg();
                                    }
                                }else {
                                        if (clusterArgumentArray[index].getCcNumber() == lastCCDesignator) {
                                            existed = true;
                                            break;
                                        } else {
                                            alternativeExists = false;
                                        }
                                }
                            }

                            if (!existed){
                                index = clusterArgumentArray[clusterArgumentArray[index].getPrevClusterArg()].getNextClusterArg();

                                while (clusterArgumentArray[index].getCcNumber() > lastCCDesignator){
                                    precedingIndex = index;
                                    precedingIndexUsed = true;
                                    index = clusterArgumentArray[index].getAlternativeArg();

                                }

                                if(clusterArgumentArray[index].getCcNumber() < lastCCDesignator && precedingIndexUsed){
                                    if (argListLength(clusterArgumentString) == 0) clusterNumber = topCongruenceClusterDesignator; // if it is the last arg string we are creating
                                    ClusterArgument clusterArgument = new ClusterArgument(0,clusterArgumentArray[index].getPrevClusterArg(), lastCCDesignator, clusterNumber, index);
                                    clusterArgumentArray[topArgStrArrIndex] = clusterArgument;
                                    clusterArgumentArray[precedingIndex].setAlternativeArg(topArgStrArrIndex);
                                    index = topArgStrArrIndex;

                                    //update ASOP for the class lastCCDesignator used here
                                    updateClassFASOP(lastCCDesignator,++level,index);

                                    topArgStrArrIndex++;
                                }
                                if(clusterArgumentArray[index].getCcNumber() < lastCCDesignator && !precedingIndexUsed){
                                    if (argListLength(clusterArgumentString) == 0) clusterNumber = topCongruenceClusterDesignator; // if it is the last arg string we are creating
                                    ClusterArgument clusterArgument = new ClusterArgument(0,clusterArgumentArray[index].getPrevClusterArg(), lastCCDesignator, clusterNumber, index);
                                    clusterArgumentArray[topArgStrArrIndex] = clusterArgument;
                                    clusterArgumentArray[clusterArgumentArray[index].getPrevClusterArg()].setNextClusterArg(topArgStrArrIndex);
                                    clusterArgumentArray[topArgStrArrIndex].setAlternativeArg(index);
                                    index = topArgStrArrIndex;

                                    //update ASOP for the class lastCCDesignator used here
                                    updateClassFASOP(lastCCDesignator,++level,index);
                                    topArgStrArrIndex++;
                                }

                            }

                        }

                    }
                }

        }
        return index;
    }

    /**
     * <p>The operation updates the clusters that ends up being with the same argument after merging the classes and put them in the list, the list is kept
     * in order </p>
     * @param lab label for the cluster we want it to join the list of clusters with same arguments
     * @param index index to the first cluster in the list of clusters with same arguments
     * @param topCongruenceClusterDesignator the top congruence cluster designator
     */
    private void updateNextWithSameArgument(Integer lab, int index, int topCongruenceClusterDesignator){
        Integer label_1 = clusterArray[clusterArgumentArray[index].getClusterNumber()].getTreeNodeLabel();//get the tree node label of the first cluster
        Integer label_2 = lab;
        int prevIndexInClusterArray = 0;
        int nextIndexInClusterArray = clusterArgumentArray[index].getClusterNumber();;
        if(label_1 < label_2){ //it is greater than the first one, make it the first one and change the argument string position
            clusterArray[topCongruenceClusterDesignator].setNextWithSameArg(clusterArgumentArray[index].getClusterNumber());
            clusterArgumentArray[index].setClusterNumber(topCongruenceClusterDesignator);
        }else{ //it is less than the first argument in the list, find the right position to insert it
            while(label_1 > label_2){
                prevIndexInClusterArray = nextIndexInClusterArray;
                nextIndexInClusterArray = clusterArray[nextIndexInClusterArray].getNextWithSameArg();
                label_1 = clusterArray[nextIndexInClusterArray].getTreeNodeLabel();

            }
            clusterArray[prevIndexInClusterArray].setNextWithSameArg(topCongruenceClusterDesignator);
            clusterArray[topCongruenceClusterDesignator].setNextWithSameArg(nextIndexInClusterArray);
        }
    }

    //update argument string occurrence position (FASOP)
    //this should be called for every congruence class designator in the cluster argument string

    /**
     * <p>The operation updates the FASOP for the classes created as they are used as arguments in the clusters, their occurrences in the cluster
     * argument array are recorded in the levels for the FASOP array</p>
     * @param ccDesignator the designator to the congruence class holding the FASOP
     * @param level level in FASOP being updated
     * @param indexInArgumentString the index in the cluster argument array where the class designated by {@param ccDesignator}
     */
    private void updateClassFASOP(int ccDesignator, int level, int indexInArgumentString){
        if (congruenceClassArray[ccDesignator].getIndexInClusterArgArrayFromASOP(level) == 0){ //for the class given go check if for the level provided index to the arg array is 0, which means no this class is at that level
            congruenceClassArray[ccDesignator].addToArgStringOccPos(indexInArgumentString,level); //just add a index to the arg array one to that level
        }else { //there is a class at that level and it has more to fix this as we have to keep the list in order of their parents
            int prevIndexToFollow = 0;
            boolean specialCase = true;
            int nextIndexToFollow = congruenceClassArray[ccDesignator].getIndexInClusterArgArrayFromASOP(level);

            if (clusterArgumentArray[indexInArgumentString].getPrevClusterArg() == clusterArgumentArray[nextIndexToFollow].getPrevClusterArg()) {

                //do nothing. last argument string position for 3 can stay the same

            } else {

                //we have to make sure the parent for the nextIndexToFollow when it is 0, exist and it is 0 otherwise this will fail
                //set the argument string at index 0, having 0's all over
                while (clusterArgumentArray[indexInArgumentString].getPrevClusterArg() < clusterArgumentArray[nextIndexToFollow].getPrevClusterArg()) {
                    prevIndexToFollow = nextIndexToFollow;
                    nextIndexToFollow = clusterArgumentArray[nextIndexToFollow].getNxtIndexWithSameCCNumberInLevel();
                    specialCase = false;
                }
                if (specialCase) { //when we have the new having the biggest father, have to update the FASOP
                    clusterArgumentArray[indexInArgumentString].setNexIndexWithSameCCInSameLevel(nextIndexToFollow);
                    congruenceClassArray[ccDesignator].addToArgStringOccPos(indexInArgumentString, level);
                } else {
                    clusterArgumentArray[prevIndexToFollow].setNexIndexWithSameCCInSameLevel(indexInArgumentString);
                    clusterArgumentArray[indexInArgumentString].setNexIndexWithSameCCInSameLevel(nextIndexToFollow);
                }
            }
        }

        //this should update last argument string position in a class to get us to the lowest level during searching.
        if(congruenceClassArray[ccDesignator].getIndexInClusterArgArrayFromASOP(level+1) == 0 ){
            congruenceClassArray[ccDesignator].setLastArgStringPosition(level);
        }else{
            //it is not the last position leave the current one
        }
    }


    //public methods to help me visualize the arrays for testing: TO BE DELETED
    public ClusterArgument[] getClusterArgArray(){return clusterArgumentArray;}
    public CongruenceCluster[] getClusterArray(){return clusterArray;}
    public Plantation[] getPlantationArray(){return plantationArray; }
    public CongruenceClass[] getCongruenceClassArray() {return congruenceClassArray;}
}
