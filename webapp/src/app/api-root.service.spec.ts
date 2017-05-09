import { TestBed, inject } from '@angular/core/testing';

import { ApiRootService } from './api-root.service';

describe('ApiRootService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ApiRootService]
    });
  });

  it('should ...', inject([ApiRootService], (service: ApiRootService) => {
    expect(service).toBeTruthy();
  }));
});
