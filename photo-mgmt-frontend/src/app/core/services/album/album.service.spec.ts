import { TestBed } from '@angular/core/testing';

import { AlbumService } from './album.service';

describe('PhotoService', () => {
  let service: AlbumService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AlbumService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
