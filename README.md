# Photo Management Backend

Actual status: I got to make a front end login page which does actually build, and serve. The problem is that I had a lot of errors.

I had to disable the dynamic path generation because in the ROUTES the defined :id parameter throw error on build:

```[ERROR] The 'photos/:id' route uses prerendering and includes parameters, but 'getPrerenderParams' is missing. Please define 'getPrerenderParams' function for this route in your server routing configuration or specify a different 'renderMode'.```

Then I had some CORS errors because of the backend, but I managed to solve them. Thien I got an invalid fetch error because after login the startup page was set to be user/info but I don't have such a page so it crashed. Currently I changed the ngOnInit to be:
```
    this.authService.login(credentials)
      .subscribe({
        next: () => {
          this.router.navigateByUrl(ROUTES.PHOTOS).then();
        },
        error: (error: HttpErrorResponse) => this.errorMessage = error.error.message
      });
```

hoping that I will get to the list of all photos after login. But when pressing the login button nothing happens. On the browser console there is no error message, and the backend shows succesfull login.
![image](https://github.com/user-attachments/assets/be240a87-8c72-4704-9a37-6ab03c328a42)
![image](https://github.com/user-attachments/assets/0a31bc53-05ed-4c32-a4ff-b04955814de9)


A Spring Boot application for managing photos in albums with user authentication, role-based access control, and basic photo editing capabilities.

## Features

- **User Authentication & Authorization**: Secure login and register using JWT with role-based access control (Admin, Moderator, User) (CURRENTLY FUNCTIONAL)
- **Album Management**: Create, view, update, and delete photo albums (CURRENTLY PARTIALLY FUNCTIONAL)
- **Photo Management**: Upload, organize, and manage photos within albums (CURRENTLY PARTIALLY FUNCTIONAL)
- **Basic Photo Editing**: Apply simple edits like brightness and contrast adjustments (UNDER DEVELOPMENT)
- **QR Code Integration**: Generate QR codes for easy album sharing (UNDER DEVELOPMENT)


##Screenshots

Prompt and result token for login
![image](https://github.com/user-attachments/assets/e308997e-1371-4902-a73d-31e2675f2d05)

Front End Login Page
![image](https://github.com/user-attachments/assets/b4f9645f-2e51-4cc6-bd22-90deffc539d0)
