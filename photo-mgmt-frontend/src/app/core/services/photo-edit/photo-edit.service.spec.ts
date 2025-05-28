import { TestBed } from '@angular/core/testing';

import { PhotoEditService } from './photo-edit.service';

describe('PhotoEditService', () => {
  let service: PhotoEditService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PhotoEditService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
