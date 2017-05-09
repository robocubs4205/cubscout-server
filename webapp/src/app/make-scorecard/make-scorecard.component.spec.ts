import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MakeScorecardComponent } from './make-scorecard.component';

describe('MakeScorecardComponent', () => {
  let component: MakeScorecardComponent;
  let fixture: ComponentFixture<MakeScorecardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MakeScorecardComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MakeScorecardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
