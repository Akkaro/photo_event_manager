// src/app/feature/photos/photo-upload/photo-upload.component.spec.ts

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { PhotoUploadComponent } from './photo-upload.component';
import { PhotoService } from '../../../core/services/photo/photo.service';
import { AlbumService } from '../../../core/services/album/album.service';
import { ModalService } from '../../../core/services/modal/modal.service';
import { ModalType } from '../../../shared/models/modal-type.enum';

describe('PhotoUploadComponent', () => {
  let component: PhotoUploadComponent;
  let fixture: ComponentFixture<PhotoUploadComponent>;
  let photoServiceSpy: jasmine.SpyObj<PhotoService>;
  let albumServiceSpy: jasmine.SpyObj<AlbumService>;
  let modalServiceSpy: jasmine.SpyObj<ModalService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    // Create spies for the services
    photoServiceSpy = jasmine.createSpyObj('PhotoService', ['save']);
    albumServiceSpy = jasmine.createSpyObj('AlbumService', ['getAll']);
    modalServiceSpy = jasmine.createSpyObj('ModalService', ['open']);
    routerSpy = jasmine.createSpyObj('Router', ['navigateByUrl']);

    // Mock service responses
    albumServiceSpy.getAll.and.returnValue(of({
      pageNumber: 0,
      pageSize: 10,
      totalPages: 1,
      totalElements: 1,
      elements: [
        { albumId: '10000000-0000-0000-0000-000000000000', albumName: 'Test Album', ownerId: '00000000-0000-0000-0000-000000000000', ownerName: 'Test User', qrCode: 'qrcode', createdAt: '2025-05-20T00:00:00Z' }
      ]
    }));

    await TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        PhotoUploadComponent
      ],
      providers: [
        FormBuilder,
        { provide: PhotoService, useValue: photoServiceSpy },
        { provide: AlbumService, useValue: albumServiceSpy },
        { provide: ModalService, useValue: modalServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PhotoUploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with required fields', () => {
    expect(component.uploadForm).toBeDefined();
    expect(component.uploadForm.get('photoName')).toBeDefined();
    expect(component.uploadForm.get('albumId')).toBeDefined();
  });

  it('should load albums on initialization', () => {
    expect(albumServiceSpy.getAll).toHaveBeenCalled();
    expect(component.albums.length).toBe(1);
    expect(component.albums[0].albumName).toBe('Test Album');
  });

  it('should set preview when file is selected', () => {
    // Mock file reader and file
    const mockFile = new File(['dummy content'], 'test-image.png', { type: 'image/png' });
    const mockFileReader = jasmine.createSpyObj('FileReader', ['readAsDataURL', 'onload']);
    spyOn(window, 'FileReader').and.returnValue(mockFileReader);

    // Create mock event
    const mockEvent = {
      target: {
        files: [mockFile]
      }
    } as unknown as Event;

    // Call the method
    component.onFileChanged(mockEvent);

    // Verify file was set
    expect(component.selectedFile).toBe(mockFile);
  });

  it('should not submit form when invalid', () => {
    // Set invalid form
    component.uploadForm.controls['photoName'].setValue('');
    component.selectedFile = null;

    // Call submit
    component.onSubmit();

    // Verify services not called
    expect(photoServiceSpy.save).not.toHaveBeenCalled();
    expect(modalServiceSpy.open).toHaveBeenCalledWith('Error', 'Please select an image to upload', ModalType.ERROR);
  });

  it('should submit valid form and handle success', () => {
    // Set valid form and file
    component.uploadForm.controls['photoName'].setValue('Test Photo');
    component.uploadForm.controls['albumId'].setValue('10000000-0000-0000-0000-000000000000');
    component.selectedFile = new File(['dummy content'], 'test-image.png', { type: 'image/png' });

    // Mock service response
    photoServiceSpy.save.and.returnValue(of({
      photoId: '20000000-0000-0000-0000-000000000000',
      photoName: 'Test Photo',
      albumId: '10000000-0000-0000-0000-000000000000',
      ownerId: '00000000-0000-0000-0000-000000000000',
      path: 'https://example.com/image.jpg',
      uploadedAt: '2025-05-20T00:00:00Z',
      isEdited: false
    }));

    // Call submit
    component.onSubmit();

    // Verify services called
    expect(photoServiceSpy.save).toHaveBeenCalled();
    expect(modalServiceSpy.open).toHaveBeenCalledWith('Success', 'Photo uploaded successfully!', ModalType.SUCCESS);
    expect(routerSpy.navigateByUrl).toHaveBeenCalled();
  });

  it('should handle error when saving photo', () => {
    // Set valid form and file
    component.uploadForm.controls['photoName'].setValue('Test Photo');
    component.uploadForm.controls['albumId'].setValue('10000000-0000-0000-0000-000000000000');
    component.selectedFile = new File(['dummy content'], 'test-image.png', { type: 'image/png' });

    // Mock service error
    const errorResponse = {
      error: { message: 'Upload failed' }
    };
    photoServiceSpy.save.and.returnValue(throwError(() => errorResponse));

    // Call submit
    component.onSubmit();

    // Verify error handling
    expect(photoServiceSpy.save).toHaveBeenCalled();
    expect(modalServiceSpy.open).toHaveBeenCalledWith('Error', 'Upload failed', ModalType.ERROR);
    expect(component.loading).toBeFalse();
  });

  it('should navigate back when cancel is clicked', () => {
    component.cancel();
    expect(routerSpy.navigateByUrl).toHaveBeenCalled();
  });
});
