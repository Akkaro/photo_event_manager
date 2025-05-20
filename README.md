# Photo Management Backend

## Actual status
I have a basic application, with all the vital functionalities, and with bugs at places.


## WHAT CAN BE DONE
Login/Register (cookies and sessions), Listing all albums, lisitng pictures of an album, uploading new picture, deleting a picture.


## Discovered bugs
Photos cannot be edited. Upon save, gives the error belove.

![image](https://github.com/user-attachments/assets/d8fe939c-adff-4650-82c7-997e7582b62d)

Photos can be uploaded as .jpg and they are treted nicely. If photos are uploaded as .bmp (extension of photos used at the IP labs), they are cropped/filled with white space.


## Upcomming TO DOs
Correct the discovered bugs(with the editing).
List only those albums which were created by the authenticated user.
Create new editing page (and finally open the topic towards IP).


A Spring Boot application for managing photos in albums with user authentication, role-based access control, and basic photo editing capabilities.

## Features

- **User Authentication & Authorization**: Secure login and register using JWT with role-based access control (Admin, Moderator, User) (CURRENTLY FUNCTIONAL)
- **Album Management**: Create, view, update, and delete photo albums (CURRENTLY PARTIALLY FUNCTIONAL)
- **Photo Management**: Upload, organize, and manage photos within albums (CURRENTLY PARTIALLY FUNCTIONAL)
- **Basic Photo Editing**: Apply simple edits like brightness and contrast adjustments (UNDER DEVELOPMENT)
- **QR Code Integration**: Generate QR codes for easy album sharing (UNDER DEVELOPMENT)


## Screenshots

Prompt and result token for login
![image](https://github.com/user-attachments/assets/e308997e-1371-4902-a73d-31e2675f2d05)

Frontend Login Page
![image](https://github.com/user-attachments/assets/bdea676f-64f5-493c-9745-dbdec1b2eeef)

Frontend Register Page
![image](https://github.com/user-attachments/assets/0746df56-3a07-4bac-a5cd-e60d6d1055eb)

Welcome Page after login
![image](https://github.com/user-attachments/assets/03d4c85b-5052-4c1b-b5e5-b89ef619ebbb)

About page for each user
![image](https://github.com/user-attachments/assets/b22dc0da-a6f4-41c3-bb0c-938e4f5c8b6d)

Page Listing all albums
![image](https://github.com/user-attachments/assets/9b29fbf4-ff24-4d17-8086-94cf6a1a86d0)

Page Listing all photos
![image](https://github.com/user-attachments/assets/e6fa3766-ddee-40c7-ab61-a641fd299985)

Page Listing photos of a single album
![image](https://github.com/user-attachments/assets/8733ce2e-feb8-466e-95ba-13ff34fa20da)

Details page of a photo
![image](https://github.com/user-attachments/assets/0198d265-0880-4ed5-a1d1-87c375332518)

Photo Upload page
![image](https://github.com/user-attachments/assets/8ad285e4-02f1-4199-ac78-71ed17437747)


