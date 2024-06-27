```
**************************************************************************************************************************
 __   __  _  __     ____                 _         ____                   _         _                 
 \ \ / / | |/ /    / ___|   __ _   ___  | |__     |  _ \    ___    __ _  (_)  ___  | |_    ___   _ __ 
  \ V /  | ' /    | |      / _` | / __| | '_ \    | |_) |  / _ \  / _` | | | / __| | __|  / _ \ | '__|
   | |   | . \    | |___  | (_| | \__ \ | | | |   |  _ <  |  __/ | (_| | | | \__ \ | |_  |  __/ | |   
   |_|   |_|\_\    \____|  \__,_| |___/ |_| |_|   |_| \_\  \___|  \__, | |_| |___/  \__|  \___| |_|   
                                                                  |___/                               
**************************************************************************************************************************
```

## Introduction

This repository contains the Proof of Concept (PoC) for YK Cash-Register, a modern POS application focused on delivering high-quality results using state-of-the-art technologies and methodologies.     

## Pre-requisites

* JDK 21+
* Maven
* Git

## How it works
A checkout process has been implemented, which currently allows to scan cart items and apply pricing rules.
Pricing rules contain a set of conditions that must be evaluated positively in order to apply their corresponding actions.

## Testing the app
CheckoutTest includes several examples that must be passed in order to validate the expected implementation.
