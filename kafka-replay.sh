#!/bin/bash

# docker rm --force  $(docker ps --filter label=ece-publisher -q)

IMAGE_TAG=auth-ece/publisher:0.1.0
counter=0


# docker rm --force  $(docker ps --filter label=electricity-publisher -q)
function publish_electricity() {
    echo "Starting up electricity publishers" 
    while [ $counter -lt $households ]
    do
        echo "Starting up electricity publisher: $counter"
        docker run --label=electricity-publisher --label=ece-publisher --network=host -d $IMAGE_TAG \
          -r $1 -d $2 -h $counter -t electricity -c $3
        ((counter++))
    done
}

# docker rm --force  $(docker ps --filter label=water-publisher -q)
function publish_water() {
    echo "Starting up water publishers" 
    while [ $counter -lt $households ]
    do
        echo "Starting up water publisher: $counter"
        docker run --label=water-publisher --label=ece-publisher --network=host -d $IMAGE_TAG \
          -r $1 -d $2 -h $counter -t water -c $3
        ((counter++))
    done
}

# docker rm --force  $(docker ps --filter label=gas-publisher -q)
function publish_gas() {
    echo "Starting up gas publishers" 
    while [ $counter -lt $households ]
    do
        echo "Starting up gas publisher: $counter"
        docker run --label=gas-publisher --label=ece-publisher --network=host -d $IMAGE_TAG \
          -r $1 -d $2 -h $counter -t gas -c $3 &
        ((counter++))
    done
}


while getopts r:d:h:t:c: flag
do
    case "${flag}" in
        r) rate=${OPTARG};;
        d) dayOffset=${OPTARG};;
        h) households=${OPTARG};;
        t) metricType=${OPTARG};;
        c) messageCount=${OPTARG};;
    esac
done

echo "metricType=$metricType"

if [ $metricType == '1' ]
    then publish_electricity $rate $dayOffset $messageCount
elif [ $metricType == '2' ]
    then publish_water $rate $dayOffset $messageCount
elif [ $metricType == '3' ]
    then publish_gas $rate $dayOffset $messageCount
else
    echo "Not a valid type, skipping"
fi
